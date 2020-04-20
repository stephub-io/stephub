package io.stephub.runtime.service;

import io.stephub.expression.AttributesContext;
import io.stephub.expression.EvaluationContext;
import io.stephub.expression.ExpressionEvaluator;
import io.stephub.expression.ParseException;
import io.stephub.expression.impl.DefaultExpressionEvaluator;
import io.stephub.json.Json;
import io.stephub.provider.StepRequest;
import org.springframework.expression.ExpressionException;
import org.springframework.stereotype.Service;

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
        stepMatch.getArguments().forEach(ma -> stepRequestBuilder.argument(ma.getName(), this.evaluateArgument(ec, ma)));
        if (stepMatch.getDocString() != null) {
            stepRequestBuilder.docString(
                    this.evaluateDocString(ec, stepMatch.getDocString()));
        }
    }

    private Json evaluateDocString(final EvaluationContext ec, final String docString) {
        // Try to match as JSON
        try {
            return this.evaluator.evaluate(docString, ec);
        } catch (final ExpressionException | ParseException e) {
            return this.evaluator.evaluate(
                    "\"" +
                            docString.replaceAll("\"", "\\\"") +
                            "\"",
                    ec);
        }
    }

    private Json evaluateArgument(final EvaluationContext ec, final GherkinPatternMatcher.ArgumentMatch argumentMatch) {
        final Json evaluatedValue = this.evaluator.evaluate(argumentMatch.getValue(), ec);
        return argumentMatch.getDesiredType().convertFrom(evaluatedValue);
    }
}
