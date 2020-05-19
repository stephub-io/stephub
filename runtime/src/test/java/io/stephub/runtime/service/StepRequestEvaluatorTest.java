package io.stephub.runtime.service;

import io.stephub.expression.CompiledExpression;
import io.stephub.expression.impl.SimpleEvaluationContext;
import io.stephub.json.Json;
import io.stephub.json.JsonBoolean;
import io.stephub.json.JsonObject;
import io.stephub.json.JsonString;
import io.stephub.json.schema.JsonSchema;
import io.stephub.provider.api.model.StepRequest;
import io.stephub.provider.api.model.spec.ArgumentSpec;
import io.stephub.provider.api.model.spec.DataTableSpec.ColumnSpec;
import io.stephub.provider.api.model.spec.DocStringSpec;
import io.stephub.runtime.service.GherkinPatternMatcher.StepMatch;
import io.stephub.runtime.service.GherkinPatternMatcher.ValueMatch;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static io.stephub.json.Json.JsonType.*;
import static io.stephub.json.schema.JsonSchema.ofType;
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
        final StepRequest.StepRequestBuilder<Json> stepBuilder = StepRequest.builder();
        this.evaluator.populateRequest(
                StepMatch.builder().docString(
                        ValueMatch.<String>builder().
                                spec(
                                        DocStringSpec.<JsonSchema>builder().
                                                schema(ofType(STRING))
                                                .build()).
                                value("Hello").build()).build(),
                stepBuilder,
                SimpleEvaluationContext.builder().build()
        );
        final StepRequest<Json> step = stepBuilder.build();
        assertThat(step.getDocString(), equalTo(
                new JsonString("Hello")
        ));
    }

    @Test
    public void testDocStringAsString() {
        final StepRequest.StepRequestBuilder<Json> stepBuilder = StepRequest.builder();
        this.evaluator.populateRequest(
                StepMatch.builder().docString(
                        ValueMatch.<String>builder().
                                spec(
                                        DocStringSpec.<JsonSchema>builder().
                                                schema(
                                                        ofType(STRING)
                                                ).build()
                                ).
                                value("\"Hel\\\"lo\"").build()
                ).build(),
                stepBuilder,
                SimpleEvaluationContext.builder().build()
        );
        final StepRequest<Json> step = stepBuilder.build();
        assertThat(step.getDocString(), equalTo(
                new JsonString("Hel\"lo")
        ));
    }

    @Test
    public void testDocStringAsJsonObj() {
        final StepRequest.StepRequestBuilder<Json> stepBuilder = StepRequest.builder();
        this.evaluator.populateRequest(
                StepMatch.builder().docString(
                        ValueMatch.<String>builder().
                                spec(
                                        DocStringSpec.<JsonSchema>builder().
                                                schema(ofType(OBJECT)).build()
                                ).
                                value(
                                        "{\n" +
                                                "\"abc\": true \n}").
                                build()).build(),
                stepBuilder,
                SimpleEvaluationContext.builder().build()
        );
        final StepRequest<Json> step = stepBuilder.build();
        assertThat(step.getDocString(), equalTo(
                JsonObject.builder().field("abc", JsonBoolean.TRUE).build()
        ));
    }

    @Test
    public void testArgEvaluationWithDesiredTypeMapping() {
        final StepRequest.StepRequestBuilder<Json> stepBuilder = StepRequest.builder();
        this.evaluator.populateRequest(
                StepMatch.builder().argument(
                        "abc",
                        ValueMatch.<CompiledExpression>builder().
                                spec(
                                        ArgumentSpec.<JsonSchema>builder().
                                                schema(
                                                        ofType(BOOLEAN)).build()).
                                value(this.evaluator.evaluator.match("{ \"some\": null }").getCompiledExpression())
                                .build()
                ).build(),
                stepBuilder,
                SimpleEvaluationContext.builder().build()
        );
        final StepRequest<Json> step = stepBuilder.build();
        assertThat(step.getArguments().get("abc"), equalTo(
                JsonBoolean.TRUE
        ));
    }

    @Test
    public void testDataTableStringColAsStringFallback() {
        this.verifyDataTableColumn(STRING, "Hello", new JsonString("Hello"));
    }

    @Test
    public void testDataTableStringColAsString() {
        this.verifyDataTableColumn(STRING, " \"Hello\" ", new JsonString("Hello"));
    }

    @Test
    public void testDataTableJsonColAsStringFallback() {
        this.verifyDataTableColumn(JSON, "Hello", new JsonString("Hello"));
    }

    @Test
    public void testDataTableJsonCol() {
        this.verifyDataTableColumn(OBJECT, "{}", new JsonObject());
    }

    @Test
    public void testDataTableJsonColDesiredAsString() {
        this.verifyDataTableColumn(STRING, "{}", STRING.convertFrom(new JsonObject()));
    }

    private void verifyDataTableColumn(final Json.JsonType givenDesiredType, final String givenColumnValue, final Json expected) {
        final StepRequest.StepRequestBuilder<Json> stepBuilder = StepRequest.builder();
        this.evaluator.populateRequest(
                StepMatch.builder().
                        dataTable(singletonList(
                                singletonMap("col1",
                                        ValueMatch.<String>builder().
                                                spec(
                                                        ColumnSpec.<JsonSchema>builder().
                                                                schema(ofType(givenDesiredType)).
                                                                build()
                                                ).
                                                value(givenColumnValue).build()
                                )
                        )).build(),
                stepBuilder,
                SimpleEvaluationContext.builder().build()
        );
        final StepRequest<Json> step = stepBuilder.build();
        assertThat(step.getDataTable(), hasSize(1));
        assertThat(step.getDataTable().get(0), aMapWithSize(1));
        assertThat(step.getDataTable().get(0), hasEntry("col1",
                expected
        ));
    }
}