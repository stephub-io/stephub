package io.stephub.runtime.service;

import io.stephub.json.Json;
import io.stephub.provider.api.model.StepResponse;
import io.stephub.runtime.model.Execution;
import io.stephub.runtime.model.ExecutionInstruction;
import io.stephub.runtime.model.RuntimeSession;
import io.stephub.runtime.model.Workspace;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ExecutionPersistence {
    Execution initExecution(Workspace workspace, ExecutionInstruction instruction, RuntimeSession.SessionSettings sessionSettings);

    void processPendingExecutionItems(String wid, String execId, WithinExecutionCommand command);

    Execution getExecution(String wid, String execId);

    @Async
    CompletableFuture<Execution> getExecution(String wid, String execId, boolean waitForCompletion);

    List<Execution> getExecutions(String wid);


    interface WithinExecutionCommand {
        void execute(Execution.ExecutionItem item, StepExecutionFacade stepExecutionFacade);
    }


    interface StepExecutionFacade {
        StepResponse<Json> doStep(Execution.StepExecutionItem item, StepExecutionItemCommand command);
    }

    interface StepExecutionItemCommand {
        StepResponse<Json> execute();
    }
}
