package org.mbok.cucumberform.providers.base;

import lombok.extern.slf4j.Slf4j;
import org.mbok.cucumberform.json.JsonBoolean;
import org.mbok.cucumberform.provider.StepResponse;
import org.mbok.cucumberform.providers.util.spring.StepMethodAnnotationProcessor.StepArgument;
import org.mbok.cucumberform.providers.util.spring.StepMethodAnnotationProcessor.StepMethod;
import org.springframework.stereotype.Component;

import static org.mbok.cucumberform.provider.StepResponse.StepStatus.FAILED;
import static org.mbok.cucumberform.provider.StepResponse.StepStatus.PASSED;

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
