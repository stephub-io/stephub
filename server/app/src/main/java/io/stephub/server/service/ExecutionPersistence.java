package io.stephub.server.service;

import io.stephub.expression.EvaluationContext;
import io.stephub.server.api.SessionExecutionContext;
import io.stephub.server.api.StepExecution;
import io.stephub.server.api.model.Execution;
import io.stephub.server.api.model.StepResponseContext;
import io.stephub.server.api.model.Workspace;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.scheduling.annotation.Async;

import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ExecutionPersistence {
    <E extends Execution> E initExecution(Workspace workspace, Execution.ExecutionStart<E> executionStart);

    void processPendingExecutionSteps(Workspace workspace, String execId, WithinExecutionStepCommand command);

    void processLoadRunner(Workspace workspace, String execId, String runnerId, WithinExecutionStepCommand command);

    Execution getExecution(String wid, String execId);

    @Async
    CompletableFuture<Execution> getExecution(String wid, String execId, boolean waitForCompletion);

    <E extends Execution> List<E> getExecutions(String wid, Class<? extends E> clazz);

    Pair<Execution.ExecutionLogAttachment, InputStream> getLogAttachment(String wid, String execId, String attachmentId);

    Duration adaptLoadRunners(String wid, String execId, LoadRunnerSpawner loadRunnerSpawner);

    void stopExecution(String wid, String execId);

    interface WithinExecutionStepCommand {
        void execute(Execution.StepExecutionItem stepItem,
                     StepExecution execution,
                     SessionExecutionContext sessionExecutionContext, EvaluationContext evaluationContext,
                     StepResponseContext responseContext);
    }

    interface LoadRunnerSpawner {
        void spawn(String runnerId);
    }

}
