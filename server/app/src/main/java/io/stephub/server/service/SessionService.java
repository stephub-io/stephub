package io.stephub.server.service;

import io.stephub.expression.AttributesContext;
import io.stephub.expression.EvaluationContext;
import io.stephub.expression.FunctionFactory;
import io.stephub.json.Json;
import io.stephub.json.JsonObject;
import io.stephub.server.api.SessionExecutionContext;
import io.stephub.server.api.model.Execution;
import io.stephub.server.api.model.ExecutionInstruction;
import io.stephub.server.api.model.RuntimeSession;
import io.stephub.server.api.model.Workspace;
import io.stephub.server.model.Context;
import io.stephub.server.service.exception.ExecutionException;
import io.stephub.server.service.exception.ExecutionPrerequisiteException;
import io.stephub.server.service.executor.ExecutorDelegate;
import io.stephub.server.service.executor.StepExecutor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.stephub.server.api.model.RuntimeSession.SessionStatus.INACTIVE;

@Slf4j
public abstract class SessionService {

    @Autowired
    private StepExecutor stepExecutor;

    @Autowired
    private FunctionFactory functionFactory;

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private ExecutionPersistence executionPersistence;

    @Autowired
    protected WorkspaceService workspaceService;

    @Autowired
    private WorkspaceValidator workspaceValidator;

    @Autowired
    private ExecutorDelegate executorDelegate;

    public abstract List<RuntimeSession> getSessions(Context ctx, String wid);

    public final RuntimeSession startSession(final Context ctx, final String wid, final RuntimeSession.SessionSettings sessionSettings) {
        final Workspace workspace = this.workspaceService.getWorkspace(ctx, wid);
        return this.startSession(workspace, sessionSettings);
    }

    public final RuntimeSession startSession(final Workspace workspace, final RuntimeSession.SessionSettings sessionSettings) {
        this.workspaceValidator.validate(workspace);
        if (workspace.getErrors() != null && !workspace.getErrors().isEmpty()) {
            throw new ExecutionException("Erroneous workspace, please correct the errors first");
        }
        final Map<String, Json> attributes = this.setUpAttributes(workspace, sessionSettings, new VariableBindingRejector() {
            private final BindException bindException = new BindException(sessionSettings, "sessionSettings");

            @Override
            public void reject(final String key, final String message) {
                this.bindException.rejectValue("variables[" + key + "]", null, message);
            }

            @Override
            public boolean hasErrors() {
                return this.bindException.hasErrors();
            }

            @Override
            public Exception buildException() {
                return this.bindException;
            }
        });
        return this.startSession(workspace, sessionSettings, attributes);
    }

    protected abstract RuntimeSession startSession(Workspace workspace, RuntimeSession.SessionSettings sessionSettings, Map<String, Json> attributes);

    public abstract void stopSession(Context ctx, String wid, String sid);

    public abstract void stopSession(RuntimeSession session);

    public abstract RuntimeSession getSession(Context ctx, String wid, String sid);

    Map<String, Json> setUpAttributes(final Workspace workspace, final RuntimeSession.SessionSettings sessionSettings, final VariableBindingRejector variableBindingRejector) {
        final Map<String, Json> attributes = new HashMap<>();
        final JsonObject vars = new JsonObject();
        final BindException varValidException = new BindException(sessionSettings, "sessionSettings");
        workspace.getVariables().forEach((key, var) -> {
            Json value = sessionSettings.getVariables().get(key);
            if (value == null) {
                value = var.getDefaultValue();
            }
            try {
                value = var.getSchema().convertFrom(value);
                var.getSchema().accept(value);
            } catch (final Exception e) {
                variableBindingRejector.reject(key, "Invalid value for variable '" + key + "': " + e.getMessage());
            }
            vars.getFields().put(key, value);
        });
        if (variableBindingRejector.hasErrors()) {
            throw new ExecutionPrerequisiteException("Invalid variable values", variableBindingRejector.buildException());
        }
        attributes.put("var", vars);
        return attributes;
    }

    public interface WithinSessionExecutor {
        void execute(Workspace workspace, RuntimeSession session, SessionExecutionContext sessionExecutionContext, EvaluationContext evaluationContext);
    }

    protected interface WithinSessionExecutorInternal {
        void execute(Workspace workspace, RuntimeSession session, SessionExecutionContext sessionExecutionContext, AttributesContext attributesContext);
    }

    protected void executeWithinSession(final String wid, final String sid, final WithinSessionExecutor executor) {
        this.executeWithinSessionInternal(wid, sid, (workspace, session, sessionExecutionContext, attributesContext) ->
                executor.execute(workspace, session, sessionExecutionContext, new EvaluationContext() {
                    @Override
                    public Json get(final String key) {
                        return attributesContext.get(key);
                    }

                    @Override
                    public void put(final String key, final Json value) {
                        attributesContext.put(key, value);
                    }

                    @Override
                    public Function createFunction(final String name) {
                        return SessionService.this.functionFactory.createFunction(name);
                    }
                }));
    }

    protected abstract void executeWithinSessionInternal(String wid, String sid, WithinSessionExecutorInternal executor);


    public final void doWithinSession(final Workspace workspace, final RuntimeSession.SessionSettings sessionSettings, final WithinSessionExecutor withinSessionExecutor) {
        final RuntimeSession session = this.startSession(workspace, sessionSettings);
        try {
            this.executeWithinSession(workspace.getId(), session.getId(), withinSessionExecutor);
        } finally {
            this.stopSession(session);
        }
    }

    public final Execution startExecution(final Context ctx, final String wid, final String sid, final ExecutionInstruction instruction) {
        final Workspace workspace = this.workspaceService.getWorkspace(ctx, wid);
        final RuntimeSession session = this.getSession(ctx, wid, sid);
        if (session.getStatus() == INACTIVE) {
            throw new ExecutionException("Session isn't active with id=" + sid);
        }
        final Execution execution = this.executionPersistence.initExecution(workspace, instruction, new RuntimeSession.SessionSettings());
        final JobKey jobKey = JobKey.jobKey(wid + "-" + sid, "executions");
        final JobDetail job = JobBuilder.newJob(SessionExecutionJob.class).withIdentity(jobKey).
                usingJobData(SessionExecutionJob.createJobDataMap(workspace, session, execution)).
                build();
        try {
            this.scheduler.scheduleJob(job, Collections.singleton(
                    TriggerBuilder.newTrigger().startNow().forJob(jobKey).build()
            ), false);
        } catch (final ObjectAlreadyExistsException e) {
            throw new ExecutionException("Multiple executions not allowed per session");
        } catch (final Exception e) {
            throw new ExecutionException("Failed to schedule execution: " + e.getMessage(), e);
        }
        return execution;
    }

    public final void execute(final String wid, final String sid, final String execId) {
        this.executeWithinSession(wid, sid, (workspace, session, sessionExecutionContext, evaluationContext) -> {
                    this.executionPersistence.processPendingExecutionItems(wid, execId,
                            (item, resultCollector) -> {
                                if (session.getStatus() == INACTIVE) {
                                    throw new ExecutionException("Session isn't active with id=" + sid);
                                }
                                log.debug("Execute {} within session={}", item, session);
                                this.executorDelegate.execute(workspace, item, sessionExecutionContext, evaluationContext, resultCollector);
                            });
                }
        );
    }

    @DisallowConcurrentExecution
    @Slf4j
    public static class SessionExecutionJob implements Job {
        @Autowired
        private SessionService sessionService;

        @Override
        public void execute(final JobExecutionContext jec) throws JobExecutionException {
            final String sid = jec.getMergedJobDataMap().getString("sid");
            final String wid = jec.getMergedJobDataMap().getString("wid");
            final String execId = jec.getMergedJobDataMap().getString("execId");
            log.debug("Executing execution={} for session={} and worksapce={}", execId, sid, wid);
            this.sessionService.execute(wid, sid, execId);
        }

        private static JobDataMap createJobDataMap(final Workspace workspace, final RuntimeSession session, final Execution execution) {
            final JobDataMap dataMap = new JobDataMap();
            dataMap.put("sid", session.getId());
            dataMap.put("wid", workspace.getId());
            dataMap.put("execId", execution.getId());
            return dataMap;
        }
    }

    public interface VariableBindingRejector {
        void reject(String key, String message);

        boolean hasErrors();

        Exception buildException();
    }
}
