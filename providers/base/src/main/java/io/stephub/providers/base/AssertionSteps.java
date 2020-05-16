package io.stephub.providers.base;

import io.stephub.json.Json;
import io.stephub.json.JsonBoolean;
import io.stephub.provider.util.StepFailedException;
import io.stephub.provider.util.spring.annotation.StepArgument;
import io.stephub.provider.util.spring.annotation.StepMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class AssertionSteps {
    @StepMethod(pattern = "assert that (?<actual>.+) is true", provider = BaseProvider.class)
    public void assertTrue(@StepArgument(name = "actual", strict = true) final JsonBoolean actual) {
        this.assertEquals(JsonBoolean.TRUE, actual);
    }

    @StepMethod(pattern = "assert that (?<actual>.+) equals (?<expected>.+)", provider = BaseProvider.class)
    public void assertTrue(@StepArgument(name = "actual") final Json actual,
                           @StepArgument(name = "expected") final Json expected) {
        this.assertEquals(expected, actual);
    }

    private void assertEquals(final Json expected, final Json actual) {
        if (!expected.equals(actual)) {
            throw new StepFailedException("Expected <" + expected + ">, but was <" + actual + ">");
        }
    }
}
