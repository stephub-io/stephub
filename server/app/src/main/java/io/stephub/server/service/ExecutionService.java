package io.stephub.server.service;

import com.github.kagkarlsson.scheduler.Scheduler;
import com.github.kagkarlsson.scheduler.task.CompletionHandler;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import com.github.kagkarlsson.scheduler.task.schedule.FixedDelay;
import io.stephub.server.api.model.Execution;
import io.stephub.server.api.model.FunctionalExecution;
import io.stephub.server.api.model.FunctionalExecution.FunctionalExecutionStart;
import io.stephub.server.api.model.LoadExecution;
import io.stephub.server.api.model.LoadExecution.LoadExecutionStart;
import io.stephub.server.api.model.Workspace;
import io.stephub.server.config.DbSchedulerConfig.MgmtTaskWrapper;
import io.stephub.server.config.DbSchedulerConfig.RunnerTaskWrapper;
import io.stephub.server.model.Context;
import io.stephub.server.service.exception.ExecutionException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindException;

import java.time.Duration;
import java.time.Instant;

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
    @Qualifier("mgmtScheduler")
    private Scheduler mgmtScheduler;

    @Autowired
    @Qualifier("runnerScheduler")
    private Scheduler runnerScheduler;

    @Autowired
    @Qualifier("functionalTask")
    private RunnerTaskWrapper<RunnerExecutionTaskData> functionalTask;

    @Autowired
    @Qualifier("loadTask")
    private RunnerTaskWrapper<RunnerExecutionTaskData> loadTask;

    @Autowired
    @Qualifier("loadRunnerAdapterTask")
    private MgmtTaskWrapper<ExecutionTaskData> loadRunnerAdapterTask;

    @Autowired
    @Qualifier("loadExecutionTerminationTask")
    public MgmtTaskWrapper<ExecutionTaskData> loadExecutionTerminationTask;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class ExecutionTaskData {
        private String wid;
        private String execId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class RunnerExecutionTaskData extends ExecutionTaskData {
        private String runnerId;

        public RunnerExecutionTaskData(final String wid, final String execId, final String runnerId) {
            super(wid, execId);
            this.runnerId = runnerId;
        }
    }

    @Configuration
    public static class ExecutionTaskConfig {
        @Autowired
        private ExecutionPersistence executionPersistence;

        @Autowired
        private ExecutionService executionService;

        @Bean("functionalTask")
        public RunnerTaskWrapper<RunnerExecutionTaskData> functionalTask() {
            return new RunnerTaskWrapper<>(Tasks.oneTime("functional-runner", RunnerExecutionTaskData.class).execute((taskInstance, ctx) -> {
                log.debug("Executing task {}", taskInstance.getData());
                this.executionService.doFunctionalExecution(taskInstance.getData());
            }));
        }

        @Bean("loadTask")
        public RunnerTaskWrapper<RunnerExecutionTaskData> loadTask() {
            return new RunnerTaskWrapper<>(Tasks.oneTime("load-runner", RunnerExecutionTaskData.class).execute((taskInstance, ctx) -> {
                log.debug("Executing task {}", taskInstance.getData());
                this.executionService.doLoadRunner(taskInstance.getData());
            }));
        }

        @Bean("loadExecutionTerminationTask")
        public MgmtTaskWrapper<ExecutionTaskData> loadExecutionTerminationTask() {
            return new MgmtTaskWrapper<>(Tasks.oneTime("load-execution-terminator", ExecutionTaskData.class).execute((taskInstance, ctx) -> {
                log.debug("Terminating execution {}", taskInstance.getData().getExecId());
                this.executionPersistence.stopExecution(taskInstance.getData().getWid(), taskInstance.getData().getExecId());
            }));
        }

        @Bean("loadRunnerAdapterTask")
        public MgmtTaskWrapper<ExecutionTaskData> loadRunnerAdapterTask() {
            return new MgmtTaskWrapper<>(Tasks.custom("load-runner-adapter", ExecutionTaskData.class).
                    onDeadExecutionRevive().
                    execute((taskInstance, ctx) -> {
                        final Duration nextAdaptationIn = this.executionPersistence.adaptLoadRunners(taskInstance.getData().getWid(), taskInstance.getData().getExecId(),
                                (runnerId) -> {
                                    this.executionService.scheduleLoadRunner(
                                            taskInstance.getData(), runnerId
                                    );
                                });
                        if (nextAdaptationIn != null) {
                            return new CompletionHandler.OnCompleteReschedule<>(FixedDelay.of(nextAdaptationIn));
                        }
                        return new CompletionHandler.OnCompleteRemove<>();
                    }));
        }
    }

    private void scheduleLoadRunner(final ExecutionTaskData data, final String runnerId) {
        log.debug("Scheduling runner={} for execution={}", runnerId, data.getExecId());
        this.runnerScheduler.schedule(this.loadTask.getTask().instance(
                data.getExecId() + "-" + runnerId,
                new RunnerExecutionTaskData(
                        data.getWid(),
                        data.getExecId(),
                        runnerId
                )), Instant.now());
    }

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
        } else if (execution instanceof LoadExecution) {
            this.initLoadExecution((LoadExecutionStart) executionStart, workspace, (LoadExecution) execution);
        }
        return execution;
    }

    public Execution stopExecution(final String wid, final String execId) {
        log.debug("Stopping execution={}", execId);
        final Execution execution = this.executionPersistence.stopExecution(wid, execId);
        if (execution instanceof LoadExecution) {
            final LoadExecution lExec = (LoadExecution) execution;
            try {
                this.mgmtScheduler.cancel(this.loadRunnerAdapterTask.getTask().instance(execution.getId()));
                this.mgmtScheduler.cancel(this.loadExecutionTerminationTask.getTask().instance(execution.getId()));
            } catch (final Exception e) {
                log.debug("Failed to cancel task for execution={} with message={}", execution, e.getMessage());
            }
            lExec.getSimulations().forEach(loadSimulation -> {
                loadSimulation.getRunners().forEach(loadRunner -> {
                    try {
                        this.runnerScheduler.cancel(
                                this.loadTask.getTask().instance(
                                        execution.getId() + "-" + loadRunner.getId()));
                    } catch (final Exception e) {
                        log.debug("Failed to cancel task={} for execution={} with message={}", execution.getId() + "-" + loadRunner.getId(), execution, e.getMessage());
                    }
                });
            });
        } else if (execution instanceof FunctionalExecution) {
            final FunctionalExecution fExec = (FunctionalExecution) execution;
            for (int i = 0; i < fExec.getRunnersCount(); i++) {
                try {
                    this.runnerScheduler.cancel(this.functionalTask.getTask().instance(execution.getId() + "-" + i));
                } catch (final Exception e) {
                    log.debug("Failed to cancel task={} for execution={} with message={}", i, execution, e.getMessage());
                }
            }
        }
        return execution;
    }

    private void initLoadExecution(final LoadExecutionStart executionStart, final Workspace workspace, final LoadExecution execution) {
        try {
            log.debug("Initializing load runner adapter for executing {}", execution);
            this.mgmtScheduler.schedule(this.loadRunnerAdapterTask.getTask().instance(execution.getId(), new ExecutionTaskData(workspace.getId(), execution.getId())), Instant.now());
            this.mgmtScheduler.schedule(this.loadExecutionTerminationTask.getTask().instance(execution.getId(), new ExecutionTaskData(workspace.getId(), execution.getId())), Instant.now().plus(executionStart.getDuration()));
        } catch (final Exception e) {
            log.error("Failed scheduling a task", e);
            throw new ExecutionException("Failed to schedule load runners for execution " + execution.getId() + ": " + e.getMessage(), e);
        }
    }

    private void initFunctionalExecution(final FunctionalExecutionStart executionStart, final Workspace workspace, final FunctionalExecution execution) {
        log.debug("Initializing {} parallel jobs for executing {}", execution.getRunnersCount(), execution);
        for (int i = 0; i < execution.getRunnersCount(); i++) {
            try {
                this.runnerScheduler.schedule(this.functionalTask.getTask().instance(execution.getId() + "-" + i, new RunnerExecutionTaskData(workspace.getId(), execution.getId(), i + "")), Instant.now());
            } catch (final Exception e) {
                log.error("Failed scheduling a task", e);
                throw new ExecutionException("Failed to schedule parallel job " + i + " for execution " + execution.getId() + ": " + e.getMessage(), e);
            }
        }
    }

    private void doLoadRunner(final RunnerExecutionTaskData taskData) {
        final Workspace workspace = this.workspaceService.getWorkspaceInternal(taskData.getWid());
        this.executionPersistence.processLoadRunner(workspace, taskData.getExecId(), taskData.getRunnerId(), (executionItem, execution, sessionExecutionContext, evaluationContext, responseContext) ->
        {
            log.debug("Execute step={} of execution={} and workspace={} on runner={}", executionItem, taskData.getExecId(), taskData.getWid(), taskData.getRunnerId());
            execution.execute(sessionExecutionContext, evaluationContext, responseContext);
        });
    }

    private void doFunctionalExecution(final RunnerExecutionTaskData taskData) {
        final Workspace workspace = this.workspaceService.getWorkspaceInternal(taskData.getWid());
        this.executionPersistence.processPendingExecutionSteps(workspace, taskData.getExecId(), (executionItem, execution, sessionExecutionContext, evaluationContext, responseContext) ->
        {
            log.debug("Execute step={} of execution={} and workspace={} on runner={}", executionItem, taskData.getExecId(), taskData.getWid(), taskData.getRunnerId());
            execution.execute(sessionExecutionContext, evaluationContext, responseContext);
        });
    }

}
