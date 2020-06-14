package io.stephub.runtime.service;

import io.stephub.runtime.model.Execution;
import io.stephub.runtime.model.ExecutionInstruction;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.CompletableFuture;

public interface ExecutionPersistence {
    Execution initExecution(String wid, ExecutionInstruction instruction);

    void doWithinExecution(String wid, String execId, WithinExecutionCommand command);

    @Async
    CompletableFuture<Execution> getExecution(String wid, String execId, boolean waitForCompletion);


    public interface WithinExecutionCommand {
        void execute(ExecutionInstruction instruction, ResultCollector resultCollector);
    }

    public interface ResultCollector {
        void collect(Execution.ExecutionResult result);
    }
}
