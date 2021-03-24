package io.stephub.server.service;

import io.stephub.expression.EvaluationContext;
import io.stephub.json.Json;
import io.stephub.json.schema.JsonSchema;
import io.stephub.provider.api.model.StepResponse;
import io.stephub.provider.api.model.spec.StepSpec;
import io.stephub.server.api.SessionExecutionContext;
import io.stephub.server.api.model.Execution;
import io.stephub.server.api.model.FunctionalExecution;
import io.stephub.server.api.model.Workspace;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.scheduling.annotation.Async;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ExecutionPersistence {
    <E extends Execution> E initExecution(Workspace workspace, Execution.ExecutionStart<E> executionStart);

    void processPendingExecutionSteps(Workspace workspace, String execId, WithinExecutionStepCommand command);

    Execution getExecution(String wid, String execId);

    @Async
    CompletableFuture<Execution> getExecution(String wid, String execId, boolean waitForCompletion);

    <E extends Execution> List<E> getExecutions(String wid, Class<? extends E> clazz);

    Pair<Execution.ExecutionLogAttachment, InputStream> getLogAttachment(String wid, String execId, String attachmentId);

    interface WithinExecutionStepCommand {
        StepExecutionResult execute(FunctionalExecution.StepExecutionItem stepItem,
                                    SessionExecutionContext sessionExecutionContext, EvaluationContext evaluationContext);
    }

    @Getter
    @Builder
    class StepExecutionResult {
        private final StepResponse<Json> response;
        private final StepSpec<JsonSchema> stepSpec;
    }

}
