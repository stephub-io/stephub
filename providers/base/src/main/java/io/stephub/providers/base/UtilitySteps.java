package io.stephub.providers.base;

import io.stephub.json.Json;
import io.stephub.json.JsonNumber;
import io.stephub.provider.api.model.LogEntry;
import io.stephub.provider.api.model.spec.PatternType;
import io.stephub.provider.util.spring.StepExecutionContext;
import io.stephub.provider.util.spring.annotation.StepArgument;
import io.stephub.provider.util.spring.annotation.StepDoc;
import io.stephub.provider.util.spring.annotation.StepDoc.StepDocExample;
import io.stephub.provider.util.spring.annotation.StepDocString;
import io.stephub.provider.util.spring.annotation.StepMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UtilitySteps {
    @StepMethod(
            pattern = "wait for {seconds} seconds", patternType = PatternType.SIMPLE, provider = BaseProvider.class,
            description = "Suspends test execution for given time period")
    public void wait(@StepArgument(name = "seconds", doc =
    @StepDoc(description = "number in seconds",
            examples = {@StepDocExample(value = "5", description = "to wait 5s"),
                    @StepDocExample(value = "0.250", description = "to wait for 250ms")})) final JsonNumber seconds,
                     final StepExecutionContext executionContext) throws InterruptedException {
        executionContext.addLog(LogEntry.builder().message("Suspending test for " + seconds + " seconds").build());
        Thread.sleep(Math.max(0, Math.round(seconds.getValue().floatValue() * 1000)));
    }

    @StepMethod(
            pattern = "a value {value}", provider = BaseProvider.class,
            description = "Evaluates and returns the value for given JSON expression from an argument",
            outputDoc = @StepDoc(description = "The evaluated value")
    )
    public Json aValue(@StepArgument(name = "value") final Json value) {
        return value;
    }

    @StepMethod(
            pattern = "a value", provider = BaseProvider.class,
            description = "Evaluates and returns the value for given JSON expression from multi-line DocString payload",
            outputDoc = @StepDoc(description = "The evaluated value")
    )
    public Json aValueAsDocString(@StepDocString(doc = @StepDoc(description = "JSON expression to evaluate and return",
            examples = {@StepDocExample(value = "[\n {\n  \"someAttribute\": \"complex\"\n }\n]",
                    description = "Passing an array of JSON objects")})) final Json value) {
        return value;
    }
}
