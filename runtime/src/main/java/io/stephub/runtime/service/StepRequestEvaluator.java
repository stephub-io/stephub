package io.stephub.runtime.service;

import io.stephub.expression.AttributesContext;
import io.stephub.expression.EvaluationContext;
import io.stephub.expression.ExpressionEvaluator;
import io.stephub.expression.ParseException;
import io.stephub.expression.impl.DefaultExpressionEvaluator;
import io.stephub.json.Json;
import io.stephub.provider.StepRequest;
import io.stephub.runtime.service.GherkinPatternMatcher.ValueMatch;
import org.springframework.expression.ExpressionException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.stephub.json.Json.JsonType.JSON;
import static io.stephub.json.Json.JsonType.STRING;

@Service
public class StepRequestEvaluator {
    private final ExpressionEvaluator evaluator = new DefaultExpressionEvaluator();

    public void populateRequest(final GherkinPatternMatcher.StepMatch stepMatch, final StepRequest.StepRequestBuilder stepRequestBuilder, final AttributesContext attributesContext) {
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
        stepMatch.getArguments().forEach((key, value) -> stepRequestBuilder.argument(key, this.evaluateArgument(ec, value)));
        if (stepMatch.getDocString() != null) {
            stepRequestBuilder.docString(
                    this.evaluateWithFallback(ec, stepMatch.getDocString()));
        }
        if (stepMatch.getDataTable() != null) {
            stepRequestBuilder.dataTable(
                    this.evaluateDataTable(ec, stepMatch.getDataTable()));
        }
    }

    private List<Map<String, Json>> evaluateDataTable(final EvaluationContext ec, final List<Map<String, ValueMatch>> dataTable) {
        return dataTable.stream().map(matchDataTable -> {
                    Map<String, Json> row = new HashMap<>();
                    matchDataTable.forEach((key, cellMatch) ->
                            row.put(key, this.evaluateWithFallback(ec, cellMatch))
                    );
                    return row;
                }
        ).collect(Collectors.toList());
    }


    private Json evaluateWithFallback(final EvaluationContext ec, final ValueMatch valueMatch) {
        final Json.JsonType desiredType = valueMatch.getDesiredSchema().getType();
        if (desiredType == STRING || desiredType == JSON) {
            try {
                // Try to evaluate as native none JSON string
                return desiredType.convertFrom(this.evaluator.evaluate(valueMatch.getValue(), ec));
            } catch (final ExpressionException | ParseException e) {
                return this.evaluator.evaluate(
                        "\"" +
                                valueMatch.getValue().replaceAll("\"", "\\\"") +
                                "\"",
                        ec);
            }
        } else {
            return desiredType.convertFrom(this.evaluator.evaluate(valueMatch.getValue(), ec));
        }
    }

    private Json evaluateArgument(final EvaluationContext ec, final ValueMatch argumentMatch) {
        final Json evaluatedValue = this.evaluator.evaluate(argumentMatch.getValue(), ec);
        return argumentMatch.getDesiredSchema().getType().convertFrom(evaluatedValue);
    }
}
