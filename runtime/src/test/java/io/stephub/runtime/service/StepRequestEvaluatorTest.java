package io.stephub.runtime.service;

import io.stephub.expression.impl.SimpleEvaluationContext;
import io.stephub.json.JsonBoolean;
import io.stephub.json.JsonObject;
import io.stephub.json.JsonString;
import io.stephub.provider.StepRequest;
import io.stephub.runtime.service.GherkinPatternMatcher.StepMatch;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {StepRequestEvaluator.class})
@Slf4j
class StepRequestEvaluatorTest {

    @Autowired
    private StepRequestEvaluator evaluator;

    @Test
    public void testDocStringAsStringFallback() {
        final StepRequest.StepRequestBuilder stepBuilder = StepRequest.builder();
        this.evaluator.populateRequest(
                StepMatch.builder().docString("Hello").build(),
                stepBuilder,
                SimpleEvaluationContext.builder().build()
        );
        final StepRequest step = stepBuilder.build();
        assertThat(step.getDocString(), equalTo(
                new JsonString("Hello")
        ));
    }

    @Test
    public void testDocStringAsString() {
        final StepRequest.StepRequestBuilder stepBuilder = StepRequest.builder();
        this.evaluator.populateRequest(
                StepMatch.builder().docString("\"Hel\\\"lo\"").build(),
                stepBuilder,
                SimpleEvaluationContext.builder().build()
        );
        final StepRequest step = stepBuilder.build();
        assertThat(step.getDocString(), equalTo(
                new JsonString("Hel\"lo")
        ));
    }

    @Test
    public void testDocStringAsJsonObj() {
        final StepRequest.StepRequestBuilder stepBuilder = StepRequest.builder();
        this.evaluator.populateRequest(
                StepMatch.builder().docString("{\n" +
                        "\"abc\": true \n}").build(),
                stepBuilder,
                SimpleEvaluationContext.builder().build()
        );
        final StepRequest step = stepBuilder.build();
        assertThat(step.getDocString(), equalTo(
                JsonObject.builder().field("abc", new JsonBoolean(true)).build()
        ));
    }
}