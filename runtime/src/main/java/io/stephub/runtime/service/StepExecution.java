package io.stephub.runtime.service;

import io.stephub.expression.EvaluationContext;
import io.stephub.json.Json;
import io.stephub.provider.api.model.StepResponse;

public interface StepExecution {
    StepResponse<Json> execute(SessionExecutionContext sessionExecutionContext, EvaluationContext evaluationContext);
}
