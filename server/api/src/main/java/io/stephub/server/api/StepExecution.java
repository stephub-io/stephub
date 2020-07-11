package io.stephub.server.api;

import io.stephub.expression.EvaluationContext;
import io.stephub.json.Json;
import io.stephub.provider.api.model.StepResponse;

import static io.stephub.provider.api.model.StepResponse.StepStatus.ERRONEOUS;

public interface StepExecution {
    StepResponse<Json> execute(SessionExecutionContext sessionExecutionContext, EvaluationContext evaluationContext);

    static StepResponse<Json> buildResponseForMissingStep(final String instruction) {
        return StepResponse.<Json>builder().status(ERRONEOUS).
                errorMessage("No step found matching the instruction '" + instruction + "'").
                build();
    }
}
