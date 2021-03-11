package io.stephub.server.service;

import io.stephub.expression.EvaluationContext;
import io.stephub.json.Json;
import io.stephub.json.schema.JsonSchema;
import io.stephub.provider.api.model.StepResponse;
import io.stephub.provider.api.model.spec.StepSpec;
import io.stephub.server.api.SessionExecutionContext;
import io.stephub.server.api.model.Execution;
import io.stephub.server.api.model.ExecutionInstruction;
import io.stephub.server.api.model.RuntimeSession;
import io.stephub.server.api.model.Workspace;
import lombok.Builder;
import lombok.Getter;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ExecutionPersistence {
    Execution initExecution(Workspace workspace, ExecutionInstruction instruction, RuntimeSession.SessionSettings sessionSettings);

    void processPendingExecutionSteps(Workspace workspace, String execId, WithinExecutionStepCommand command);

    Execution getExecution(String wid, String execId);

    @Async
    CompletableFuture<Execution> getExecution(String wid, String execId, boolean waitForCompletion);

    List<Execution> getExecutions(String wid);


    interface WithinExecutionStepCommand {
        StepExecutionResult execute(Execution.StepExecutionItem stepItem,
                                   SessionExecutionContext sessionExecutionContext, EvaluationContext evaluationContext);
    }

    @Getter
    @Builder
    class StepExecutionResult {
        private final StepResponse<Json> response;
        private final StepSpec<JsonSchema> stepSpec;
    }

    interface StepExecutionItemCommand {
        StepExecutionResult execute();
    }
}
