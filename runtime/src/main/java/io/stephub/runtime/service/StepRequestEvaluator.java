package io.stephub.runtime.service;

import io.stephub.expression.*;
import io.stephub.expression.impl.DefaultExpressionEvaluator;
import io.stephub.json.Json;
import io.stephub.json.JsonException;
import io.stephub.json.schema.JsonSchema;
import io.stephub.provider.api.model.StepRequest;
import io.stephub.provider.api.model.spec.ValueSpec;
import io.stephub.runtime.service.GherkinPatternMatcher.ValueMatch;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.expression.ExpressionException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.stephub.json.Json.JsonType.ANY;
import static io.stephub.json.Json.JsonType.STRING;

@Service
public class StepRequestEvaluator {
    final ExpressionEvaluator evaluator = new DefaultExpressionEvaluator();

    public void populateRequest(final GherkinPatternMatcher.StepMatch stepMatch, final StepRequest.StepRequestBuilder<Json, ?, ?> stepRequestBuilder, final AttributesContext attributesContext) {
        final EvaluationContext ec = new EvaluationContext() {
            @Override
            public Json get(final String key) {
                return attributesContext.get(key);
            }

            @Override
            public Function createFunction(final String name) {
                // TODO
                return null;
            }
        };
        stepMatch.getArguments().forEach((key, value) -> stepRequestBuilder.argument(key, this.evaluateCompiledValue("argument '" + key + "'", ec, value)));
        if (stepMatch.getDocString() != null) {
            stepRequestBuilder.docString(
                    this.evaluateWithFallback("DocString", ec, stepMatch.getDocString()));
        }
        if (stepMatch.getDataTable() != null) {
            stepRequestBuilder.dataTable(
                    this.evaluateDataTable(ec, stepMatch.getDataTable()));
        }
    }

    private List<Map<String, Json>> evaluateDataTable(final EvaluationContext ec, final List<Map<String, ValueMatch<String>>> dataTable) {
        return dataTable.stream().map(matchDataTable -> {
                    Map<String, Json> row = new HashMap<>();
                    matchDataTable.forEach((key, cellMatch) ->
                            row.put(key, this.evaluateWithFallback("row '" + row.size() + "' and col '" + key + "' in DataTable", ec, cellMatch))
                    );
                    return row;
                }
        ).collect(Collectors.toList());
    }


    private Json evaluateWithFallback(final String valueContext, final EvaluationContext ec, final ValueMatch<String> valueMatch) {
        final List<Json.JsonType> desiredTypes = valueMatch.getSpec().getSchema().getTypes();
        if (desiredTypes.contains(STRING) || desiredTypes.contains(ANY)) {
            try {
                // Try to evaluate as native none JSON string
                return valueMatch.getSpec().getSchema().convertFrom(this.evaluator.evaluate(valueMatch.getValue(), ec));
            } catch (final ExpressionException | ParseException | JsonException e) {
                return this.evaluator.evaluate(
                        "\"" +
                                valueMatch.getValue().replaceAll("\"", "\\\"") +
                                "\"",
                        ec);
            }
        } else {
            return this.evaluateStringValue(valueContext, ec, valueMatch);
        }
    }

    private Json evaluateStringValue(final String valueContext, final EvaluationContext ec, final ValueMatch<String> valueMatch) {
        return this.convert(valueContext, this.evaluator.evaluate(valueMatch.getValue(), ec), valueMatch.getSpec());
    }

    private Json evaluateCompiledValue(final String valueContext, final EvaluationContext ec, final ValueMatch<CompiledExpression> valueMatch) {
        return this.convert(valueContext, this.evaluator.evaluate(valueMatch.getValue(), ec), valueMatch.getSpec());
    }

    private Json convert(final String valueContext, final Json evaluatedValue, final ValueSpec<JsonSchema> spec) {
        if (spec.isStrict()) {
            final Json.JsonType givenType = Json.JsonType.valueOf(evaluatedValue);
            final List<Json.JsonType> expectedTypes = spec.getSchema().getTypes();
            for (final Json.JsonType expectedType : expectedTypes) {
                if (givenType == expectedType) {
                    return evaluatedValue;
                }
            }
            throw new ExpressionException("Expected types for " + valueContext + " are '" + ArrayUtils.toString(expectedTypes) + "', but was '" + givenType + "'");
        } else {
            return spec.getSchema().convertFrom(evaluatedValue);
        }
    }
}
