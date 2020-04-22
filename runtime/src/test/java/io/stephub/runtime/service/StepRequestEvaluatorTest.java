package io.stephub.runtime.service;

import io.stephub.expression.impl.SimpleEvaluationContext;
import io.stephub.json.JsonBoolean;
import io.stephub.json.JsonObject;
import io.stephub.json.JsonString;
import io.stephub.provider.StepRequest;
import io.stephub.runtime.service.GherkinPatternMatcher.StepMatch;
import io.stephub.runtime.service.GherkinPatternMatcher.ValueMatch;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static io.stephub.json.Json.JsonType.*;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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
                StepMatch.builder().docString(
                        ValueMatch.builder().
                                desiredType(STRING).
                                value("Hello").build()).build(),
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
                StepMatch.builder().docString(
                        ValueMatch.builder().
                                desiredType(STRING).
                                value("\"Hel\\\"lo\"").build()
                ).build(),
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
                StepMatch.builder().docString(
                        ValueMatch.builder().desiredType(OBJECT).
                                value(
                                        "{\n" +
                                                "\"abc\": true \n}").
                                build()).build(),
                stepBuilder,
                SimpleEvaluationContext.builder().build()
        );
        final StepRequest step = stepBuilder.build();
        assertThat(step.getDocString(), equalTo(
                JsonObject.builder().field("abc", new JsonBoolean(true)).build()
        ));
    }

    @Test
    public void testArgEvaluationWithDesiredTypeMapping() {
        final StepRequest.StepRequestBuilder stepBuilder = StepRequest.builder();
        this.evaluator.populateRequest(
                StepMatch.builder().argument(
                        "abc",
                        ValueMatch.builder().
                                desiredType(BOOLEAN).value("{ \"some\": null }")
                                .build()
                ).build(),
                stepBuilder,
                SimpleEvaluationContext.builder().build()
        );
        final StepRequest step = stepBuilder.build();
        assertThat(step.getArguments().get("abc"), equalTo(
                new JsonBoolean(true)
        ));
    }

    @Test
    public void testDataTableAsStringFallback() {
        final StepRequest.StepRequestBuilder stepBuilder = StepRequest.builder();
        this.evaluator.populateRequest(
                StepMatch.builder().
                        dataTable(singletonList(
                                singletonMap("col1",
                                        ValueMatch.builder().
                                                desiredType(STRING).
                                                value("Hello").build()
                                )
                        )).build(),
                stepBuilder,
                SimpleEvaluationContext.builder().build()
        );
        final StepRequest step = stepBuilder.build();
        assertThat(step.getDataTable(), hasSize(1));
        assertThat(step.getDataTable().get(0), aMapWithSize(1));
        assertThat(step.getDataTable().get(0), hasEntry("col1",
                new JsonString("Hello")
        ));
    }
}