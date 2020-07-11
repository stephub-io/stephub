package io.stephub.server.service;

import io.stephub.server.api.model.Execution;
import io.stephub.server.api.model.Workspace;
import io.stephub.server.model.Context;
import io.stephub.server.service.exception.ExecutionException;
import io.stephub.server.service.executor.ExecutorDelegate;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private ExecutorDelegate executorDelegate;


    public Execution startExecution(final Context ctx,
                                    final String wid,
                                    final Execution.ExecutionStart executionStart) {
        final Workspace workspace = this.workspaceService.getWorkspace(ctx, wid);
        this.workspaceValidator.validate(workspace);
        if (workspace.getErrors() != null && !workspace.getErrors().isEmpty()) {
            throw new ExecutionException("Erroneous workspace, please correct the errors first");
        }
        this.sessionService.setUpAttributes(workspace, executionStart.getSessionSettings());
        final Execution execution = this.executionPersistence.initExecution(workspace, executionStart.getInstruction(), executionStart.getSessionSettings());
        final int parallelizationCount = Math.min(execution.getMaxParallelizationCount(), executionStart.getParallelSessionCount());
        log.debug("Initializing {} parallel jobs for executing {}", parallelizationCount, execution);
        for (int i = 0; i < parallelizationCount; i++) {
            final JobKey jobKey = JobKey.jobKey(execution.getId() + "-" + i, "executions");
            final JobDetail job = JobBuilder.newJob(ParallelExecutionJob.class).withIdentity(jobKey).
                    usingJobData(ParallelExecutionJob.createJobDataMap(workspace, execution)).
                    build();
            try {
                this.scheduler.scheduleJob(job, Collections.singleton(
                        TriggerBuilder.newTrigger().startNow().forJob(jobKey).build()
                ), false);
            } catch (final Exception e) {
                throw new ExecutionException("Failed to schedule parallel job " + i + " for execution " + execution.getId() + ": " + e.getMessage(), e);
            }
        }
        return execution;
    }

    private void doExecution(final String wid, final String execId) {
        final Execution execution = this.executionPersistence.getExecution(wid, execId);
        final Workspace workspace = this.workspaceService.getWorkspaceInternal(wid);
        this.sessionService.doWithinSession(workspace, execution.getSessionSettings(),
                (workspace1, session, sessionExecutionContext, evaluationContext) ->
                        this.executionPersistence.processPendingExecutionItems(wid, execId, (executionItem, resultCollector) ->
                        {
                            log.debug("Execute item={} of execution={} within session={}", executionItem, execution, session);
                            this.executorDelegate.execute(workspace, executionItem, sessionExecutionContext, evaluationContext, resultCollector);
                        })
        );
    }

    @Slf4j
    public static class ParallelExecutionJob implements Job {
        @Autowired
        private ExecutionService executionService;

        @Override
        public void execute(final JobExecutionContext jec) throws JobExecutionException {
            final String wid = jec.getMergedJobDataMap().getString("wid");
            final String execId = jec.getMergedJobDataMap().getString("execId");
            log.debug("Executing execution={} for workspace={}", execId, wid);
            this.executionService.doExecution(wid, execId);
        }

        private static JobDataMap createJobDataMap(final Workspace workspace, final Execution execution) {
            final JobDataMap dataMap = new JobDataMap();
            dataMap.put("wid", workspace.getId());
            dataMap.put("execId", execution.getId());
            return dataMap;
        }
    }
}
