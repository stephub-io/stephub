package io.stephub.server.service;

import io.stephub.server.api.model.Execution;
import io.stephub.server.api.model.FunctionalExecution;
import io.stephub.server.api.model.FunctionalExecution.FunctionalExecutionStart;
import io.stephub.server.api.model.LoadExecution;
import io.stephub.server.api.model.Workspace;
import io.stephub.server.model.Context;
import io.stephub.server.service.exception.ExecutionException;
import io.stephub.server.service.executor.StepExecutor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindException;

import java.util.Collections;

@Slf4j
@Service
public class ExecutionService {
    @Autowired
    private ExecutionPersistence executionPersistence;

    @Autowired
    private WorkspaceService workspaceService;

    @Autowired
    private WorkspaceValidator workspaceValidator;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private Scheduler scheduler;


    @Autowired
    private StepExecutor stepExecutor;


    public <E extends Execution> E startExecution(final Context ctx,
                                                  final String wid,
                                                  final Execution.ExecutionStart<E> executionStart) {
        final Workspace workspace = this.workspaceService.getWorkspace(ctx, wid);
        this.workspaceValidator.validate(workspace);
        if (workspace.getErrors() != null && !workspace.getErrors().isEmpty()) {
            throw new ExecutionException("Erroneous workspace, please correct the errors first");
        }
        this.sessionService.setUpAttributes(workspace, executionStart.getSessionSettings(), new SessionService.VariableBindingRejector() {
            private final BindException bindException = new BindException(executionStart, "executionStart");

            @Override
            public void reject(final String key, final String message) {
                this.bindException.rejectValue("sessionSettings.variables[" + key + "]", null, message);
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
        final E execution = this.executionPersistence.initExecution(workspace, executionStart);
        if (execution instanceof FunctionalExecution) {
            this.initFunctionalExecution((FunctionalExecutionStart) executionStart, workspace, (FunctionalExecution) execution);
        }
        return execution;
    }

    private void initLoadExecution(final LoadExecution.LoadExecutionStart executionStart, final Workspace workspace, final LoadExecution execution) {
        
    }

    private void initFunctionalExecution(final FunctionalExecutionStart executionStart, final Workspace workspace, final FunctionalExecution execution) {
        final int parallelizationCount = Math.min(execution.getMaxParallelizationCount(), executionStart.getParallelSessionCount());
        log.debug("Initializing {} parallel jobs for executing {}", parallelizationCount, execution);
        for (int i = 0; i < parallelizationCount; i++) {
            final JobKey jobKey = JobKey.jobKey(execution.getId() + "-" + i, "executions");
            final JobDetail job = JobBuilder.newJob(ParallelExecutionJob.class).withIdentity(jobKey).
                    usingJobData(ParallelExecutionJob.createJobDataMap(workspace, execution, i)).
                    build();
            try {
                this.scheduler.scheduleJob(job, Collections.singleton(
                        TriggerBuilder.newTrigger().startNow().forJob(jobKey).build()
                ), false);
            } catch (final Exception e) {
                throw new ExecutionException("Failed to schedule parallel job " + i + " for execution " + execution.getId() + ": " + e.getMessage(), e);
            }
        }
    }

    private void doFunctionalExecution(final String wid, final String execId) {
        final FunctionalExecution execution = (FunctionalExecution) this.executionPersistence.getExecution(wid, execId);
        final Workspace workspace = this.workspaceService.getWorkspaceInternal(wid);
        this.executionPersistence.processPendingExecutionSteps(workspace, execId, (executionItem, sessionExecutionContext, evaluationContext) ->
        {
            log.debug("Execute step={} of execution={} and workspace={}", execId, execution, wid);
            return this.stepExecutor.execute(workspace, executionItem, sessionExecutionContext, evaluationContext);
        });
    }


    @Slf4j
    @DisallowConcurrentExecution
    public static class ParallelExecutionJob implements Job {
        @Autowired
        private ExecutionService executionService;

        @Override
        public void execute(final JobExecutionContext jec) throws JobExecutionException {
            final String wid = jec.getMergedJobDataMap().getString("wid");
            final String execId = jec.getMergedJobDataMap().getString("execId");
            final int runner = jec.getMergedJobDataMap().getInt("runner");
            log.debug("Executing execution={} for workspace={} on runner={}", execId, wid, runner);
            this.executionService.doFunctionalExecution(wid, execId);
        }

        private static JobDataMap createJobDataMap(final Workspace workspace, final FunctionalExecution execution, final int runner) {
            final JobDataMap dataMap = new JobDataMap();
            dataMap.put("wid", workspace.getId());
            dataMap.put("runner", runner);
            dataMap.put("execId", execution.getId());
            return dataMap;
        }
    }
}
