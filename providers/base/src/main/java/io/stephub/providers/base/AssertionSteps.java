package io.stephub.providers.base;

import lombok.extern.slf4j.Slf4j;
import io.stephub.json.JsonBoolean;
import io.stephub.provider.StepResponse;
import io.stephub.providers.util.spring.StepMethodAnnotationProcessor.StepArgument;
import io.stephub.providers.util.spring.StepMethodAnnotationProcessor.StepMethod;
import org.springframework.stereotype.Component;

import static io.stephub.provider.StepResponse.StepStatus.FAILED;
import static io.stephub.provider.StepResponse.StepStatus.PASSED;

@Component
@Slf4j
public class AssertionSteps {
    @StepMethod(pattern = "assert the condition (?<condition>.+) is true", provider = BaseProvider.class)
    public StepResponse assertTrue(@StepArgument(name = "condition") final JsonBoolean conditionResult) {
        final StepResponse.StepResponseBuilder responseBuilder = StepResponse.builder();
        responseBuilder.status(conditionResult.isValue() ? PASSED : FAILED);
        return responseBuilder.build();
    }
}
