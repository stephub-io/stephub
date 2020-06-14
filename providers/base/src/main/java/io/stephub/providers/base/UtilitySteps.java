package io.stephub.providers.base;

import io.stephub.json.JsonNumber;
import io.stephub.provider.api.model.spec.PatternType;
import io.stephub.provider.util.spring.annotation.StepArgument;
import io.stephub.provider.util.spring.annotation.StepMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UtilitySteps {
    @StepMethod(pattern = "wait for {seconds} seconds", patternType = PatternType.SIMPLE, provider = BaseProvider.class)
    public void assertTrue(@StepArgument(name = "seconds") final JsonNumber seconds) throws InterruptedException {
        Thread.sleep(seconds.getValue().intValue() * 1000);
    }
}
