package io.stephub.runtime.service;

import io.stephub.expression.EvaluationContext;
import io.stephub.expression.FunctionFactory;
import io.stephub.json.Json;
import io.stephub.json.JsonNull;
import io.stephub.json.JsonObject;
import io.stephub.json.schema.JsonInvalidSchemaException;
import io.stephub.provider.api.model.StepResponse;
import io.stephub.runtime.model.Context;
import io.stephub.runtime.model.Execution;
import io.stephub.runtime.model.ExecutionInstruction;
import io.stephub.runtime.model.ExecutionInstruction.StepExecutionInstruction;
import io.stephub.runtime.model.RuntimeSession;
import io.stephub.runtime.service.exception.ExecutionException;
import io.stephub.runtime.service.exception.ExecutionPrerequisiteException;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.stephub.provider.api.model.StepResponse.StepStatus.ERRONEOUS;
import static io.stephub.runtime.model.RuntimeSession.SessionStatus.INACTIVE;

@Slf4j
public abstract class SessionService {
    @Autowired
    private StepExecutionResolver stepExecutionResolver;

    @Autowired
    private FunctionFactory functionFactory;

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private ExecutionPersistence executionPersistence;

    public abstract List<RuntimeSession> getSessions(Context ctx, String wid);

    public abstract RuntimeSession startSession(Context ctx, String wid, RuntimeSession.SessionStart sessionStart);

    public abstract void stopSession(Context ctx, String wid, String sid);

    public abstract RuntimeSession getSession(Context ctx, String wid, String sid);

    protected void setUpAttributes(final RuntimeSession session, final RuntimeSession.SessionStart sessionStart) {
        final Map<String, Json> attributes = new HashMap<>();
        final JsonObject vars = new JsonObject();
        session.getWorkspace().getVariables().forEach((key, var) -> {
            Json value = sessionStart.getVariables().get(key);
            if (value == null) {
                if (var.getValue() != JsonNull.INSTANCE) {
                    value = var.getValue();
                } else {
                    value = var.getDefaultValue();
                }
            }
            try {
                var.getSchema().accept(value);
            } catch (final JsonInvalidSchemaException e) {
                throw new ExecutionPrerequisiteException("Invalid value for variable '" + key + "': " + e.getMessage());
            }
            vars.getFields().put(key, value);
        });
        session.getAttributes().put("var", vars);
    }

    public interface WithinSessionExecutor {
        void execute(RuntimeSession session, SessionExecutionContext sessionExecutionContext);
    }

    protected abstract void executeWithinSession(String wid, String sid, String execId, WithinSessionExecutor executor);

    public Execution startExecution(final Context ctx, final String wid, final String sid, final ExecutionInstruction instruction) {
        final RuntimeSession session = this.getSession(ctx, wid, sid);
        if (session.getStatus() == INACTIVE) {
            throw new ExecutionException("Session isn't active with id=" + sid);
        }
        final Execution execution = this.executionPersistence.initExecution(wid, instruction);
        final JobKey jobKey = JobKey.jobKey(wid + "-" + sid, "executions");
        final JobDetail job = JobBuilder.newJob(SessionExecutionJob.class).withIdentity(jobKey).
                usingJobData(SessionExecutionJob.createJobDataMap(session, execution)).
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

    private void execute(final String wid, final String sid, final String execId) {
        this.executeWithinSession(wid, sid, execId, (session, sessionExecutionContext) -> {
                    this.executionPersistence.doWithinExecution(wid, execId,
                            (instruction, resultCollector) -> {
                                if (session.getStatus() == INACTIVE) {
                                    throw new ExecutionException("Session isn't active with id=" + sid);
                                }
                                final EvaluationContext evaluationContext = new EvaluationContext() {
                                    @Override
                                    public Json get(final String key) {
                                        return session.getAttributes().get(key);
                                    }

                                    @Override
                                    public void put(final String key, final Json value) {
                                        session.getAttributes().put(key, value);
                                    }

                                    @Override
                                    public Function createFunction(final String name) {
                                        return SessionService.this.functionFactory.createFunction(name);
                                    }
                                };
                                log.debug("Execute {} within session={}", instruction, session);
                                if (instruction instanceof StepExecutionInstruction) {
                                    final StepExecutionInstruction stepInstruction = (StepExecutionInstruction) instruction;
                                    try {
                                        final StepExecution stepExecution = this.stepExecutionResolver.resolveStepExecution(stepInstruction.getInstruction(), session.getWorkspace());
                                        if (stepExecution == null) {
                                            resultCollector.collect(Execution.StepExecutionResult.builder().instruction(stepInstruction.getInstruction()).
                                                    response(buildResponseForMissingStep(stepInstruction.getInstruction())).
                                                    build());
                                        } else {
                                            resultCollector.collect(
                                                    Execution.StepExecutionResult.builder().instruction(stepInstruction.getInstruction()).
                                                            response(stepExecution.execute(sessionExecutionContext, evaluationContext)).
                                                            build()
                                            );
                                        }
                                    } catch (final Exception e) {
                                        log.error("Unexpected step execution error", e);
                                        resultCollector.collect(Execution.StepExecutionResult.builder().instruction(stepInstruction.getInstruction()).
                                                response(StepResponse.<Json>builder().status(ERRONEOUS).
                                                        errorMessage("Unexpected exception: " + e.getMessage()).
                                                        build()).
                                                build());
                                    }
                                }
                            });
                }
        );
    }

    public static final StepResponse<Json> buildResponseForMissingStep(final String instruction) {
        return StepResponse.<Json>builder().status(ERRONEOUS).
                errorMessage("No step found matching the instruction '" + instruction + "'").
                build();
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

        private static JobDataMap createJobDataMap(final RuntimeSession session, final Execution execution) {
            final JobDataMap dataMap = new JobDataMap();
            dataMap.put("sid", session.getId());
            dataMap.put("wid", session.getWorkspace().getId());
            dataMap.put("execId", execution.getId());
            return dataMap;
        }
    }
}
