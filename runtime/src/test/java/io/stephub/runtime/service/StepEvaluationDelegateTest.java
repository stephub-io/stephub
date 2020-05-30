package io.stephub.runtime.service;

import io.stephub.expression.CompiledExpression;
import io.stephub.expression.impl.SimpleEvaluationContext;
import io.stephub.json.Json;
import io.stephub.json.JsonBoolean;
import io.stephub.json.JsonObject;
import io.stephub.json.JsonString;
import io.stephub.json.schema.JsonSchema;
import io.stephub.provider.api.model.StepRequest;
import io.stephub.provider.api.model.StepResponse;
import io.stephub.provider.api.model.spec.ArgumentSpec;
import io.stephub.provider.api.model.spec.DataTableSpec.ColumnSpec;
import io.stephub.provider.api.model.spec.DocStringSpec;
import io.stephub.runtime.service.GherkinPatternMatcher.StepMatch;
import io.stephub.runtime.service.GherkinPatternMatcher.ValueMatch;
import io.stephub.runtime.service.StepEvaluationDelegate.StepEvaluation;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;

import static io.stephub.json.Json.JsonType.*;
import static io.stephub.json.schema.JsonSchema.ofType;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {StepEvaluationDelegate.class})
@Slf4j
class StepEvaluationDelegateTest {

    @Autowired
    private StepEvaluationDelegate evaluator;

    @Test
    public void testDocStringAsStringFallback() {
        final StepEvaluation stepEvaluation = this.evaluator.getStepEvaluation(
                StepMatch.builder().docString(
                        ValueMatch.<String>builder().
                                spec(
                                        DocStringSpec.<JsonSchema>builder().
                                                schema(ofType(STRING))
                                                .build()).
                                value("Hello").build()).build(),
                SimpleEvaluationContext.builder().build()
        );
        final StepRequest<Json> step = stepEvaluation.getRequestBuilder().build();
        assertThat(step.getDocString(), equalTo(
                new JsonString("Hello")
        ));
    }

    @Test
    public void testDocStringAsString() {
        final StepEvaluation stepEvaluation = this.evaluator.getStepEvaluation(
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
                SimpleEvaluationContext.builder().build()
        );
        final StepRequest<Json> step = stepEvaluation.getRequestBuilder().build();
        assertThat(step.getDocString(), equalTo(
                new JsonString("Hel\"lo")
        ));
    }

    @Test
    public void testDocStringAsJsonObj() {
        final StepEvaluation stepEvaluation = this.evaluator.getStepEvaluation(
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
                SimpleEvaluationContext.builder().build()
        );
        final StepRequest<Json> step = stepEvaluation.getRequestBuilder().build();
        assertThat(step.getDocString(), equalTo(
                JsonObject.builder().field("abc", JsonBoolean.TRUE).build()
        ));
    }

    @Test
    public void testArgEvaluationWithDesiredTypeMapping() {
        final StepEvaluation stepEvaluation = this.evaluator.getStepEvaluation(
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
                SimpleEvaluationContext.builder().build()
        );
        final StepRequest<Json> step = stepEvaluation.getRequestBuilder().build();
        assertThat(step.getArguments().get("abc"), equalTo(
                JsonBoolean.TRUE
        ));
    }

    @Test
    public void testOutputAssignment() {
        final SimpleEvaluationContext ec = new SimpleEvaluationContext(new HashMap<>(), new HashMap<>());
        final StepEvaluation stepEvaluation = this.evaluator.getStepEvaluation(
                StepMatch.builder().argument(
                        "abc",
                        ValueMatch.<CompiledExpression>builder().
                                spec(
                                        ArgumentSpec.<JsonSchema>builder().
                                                schema(
                                                        ofType(BOOLEAN)).build()).
                                value(this.evaluator.evaluator.match("true").getCompiledExpression())
                                .build()
                ).outputAssignmentAttribute("response").build(),
                ec
        );
        final StepRequest<Json> step = stepEvaluation.getRequestBuilder().build();
        assertThat(step.getArguments().get("abc"), equalTo(
                JsonBoolean.TRUE
        ));
        stepEvaluation.postEvaluateResponse(StepResponse.<Json>builder().output(
                new JsonString("hello")
        ).build());
        assertThat(ec.get("response"), equalTo(new JsonString("hello")));
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
        this.verifyDataTableColumn(ANY, "Hello", new JsonString("Hello"));
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
        final StepEvaluation stepEvaluation = this.evaluator.getStepEvaluation(
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
                SimpleEvaluationContext.builder().build()
        );
        final StepRequest<Json> step = stepEvaluation.getRequestBuilder().build();
        assertThat(step.getDataTable(), hasSize(1));
        assertThat(step.getDataTable().get(0), aMapWithSize(1));
        assertThat(step.getDataTable().get(0), hasEntry("col1",
                expected
        ));
    }
}