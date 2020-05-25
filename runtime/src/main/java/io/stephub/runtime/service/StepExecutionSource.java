package io.stephub.runtime.service;

import io.stephub.runtime.model.StepInstruction;
import io.stephub.runtime.model.Workspace;

public interface StepExecutionSource {
    StepExecution resolveStepExecution(StepInstruction stepInstruction, Workspace workspace);
}
