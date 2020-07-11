package io.stephub.server.service;

import io.stephub.server.api.StepExecution;
import io.stephub.server.api.model.Workspace;

public interface StepExecutionSource {
    StepExecution resolveStepExecution(String stepInstruction, Workspace workspace);
}
