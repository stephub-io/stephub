package io.stephub.runtime.service;

import io.stephub.runtime.model.Workspace;

public interface StepExecutionSource {
    StepExecution resolveStepExecution(String stepInstruction, Workspace workspace);
}
