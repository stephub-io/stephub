package io.stephub.server.api;

import io.stephub.expression.EvaluationContext;
import io.stephub.json.Json;
import io.stephub.json.schema.JsonSchema;
import io.stephub.provider.api.model.StepResponse;
import io.stephub.provider.api.model.spec.StepSpec;
import io.stephub.server.api.model.StepResponseContext;

import java.time.Duration;

import static io.stephub.provider.api.model.StepResponse.StepStatus.ERRONEOUS;

public interface StepExecution {

    void execute(SessionExecutionContext sessionExecutionContext, EvaluationContext evaluationContext, StepResponseContext responseContext);

    StepSpec<JsonSchema> getStepSpec();

    static StepResponse<Json> buildResponseForMissingStep(final String instruction) {
        return StepResponse.<Json>builder().status(ERRONEOUS).
                duration(Duration.ofMinutes(0)).
                errorMessage("No step found matching the instruction '" + instruction + "'").
                build();
    }
}
