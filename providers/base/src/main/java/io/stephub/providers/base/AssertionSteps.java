package io.stephub.providers.base;

import io.stephub.json.Json;
import io.stephub.json.JsonBoolean;
import io.stephub.provider.api.model.StepResponse;
import io.stephub.provider.util.spring.annotation.StepArgument;
import io.stephub.provider.util.spring.annotation.StepMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static io.stephub.provider.api.model.StepResponse.StepStatus.FAILED;
import static io.stephub.provider.api.model.StepResponse.StepStatus.PASSED;


@Component
@Slf4j
public class AssertionSteps {
    @StepMethod(pattern = "assert the condition (?<condition>.+) is true", provider = BaseProvider.class)
    public StepResponse<Json> assertTrue(@StepArgument(name = "condition") final JsonBoolean conditionResult) {
        final StepResponse.StepResponseBuilder<Json> responseBuilder = StepResponse.<Json>builder();
        responseBuilder.status(conditionResult.isTrue() ? PASSED : FAILED);
        return responseBuilder.build();
    }
}
