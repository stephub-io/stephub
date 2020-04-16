package io.stephub.runtime.service;

import io.stephub.expression.AttributesContext;
import io.stephub.expression.EvaluationContext;
import io.stephub.expression.ExpressionEvaluator;
import io.stephub.expression.impl.DefaultExpressionEvaluator;
import io.stephub.json.Json;
import io.stephub.provider.Argument;
import io.stephub.provider.StepRequest;
import org.springframework.stereotype.Service;

@Service
public class StepRequestEvaluator {
    private ExpressionEvaluator evaluator = new DefaultExpressionEvaluator();

    public void populateRequest(GherkinPatternMatcher.StepMatch stepMatch, StepRequest.StepRequestBuilder stepRequestBuilder, AttributesContext attributesContext) {
        EvaluationContext ec = new EvaluationContext() {
            @Override
            public Json get(String key) {
                return attributesContext.get(key);
            }

            @Override
            public Function createFunction(String name) {
                // TODO
                return null;
            }
        };
        stepMatch.getArguments().forEach(ma -> stepRequestBuilder.argument(evaluateArgument(ec, ma)));
    }

    private Argument evaluateArgument(EvaluationContext ec, GherkinPatternMatcher.ArgumentMatch argumentMatch) {
        Json evaluatedValue = evaluator.evaluate(argumentMatch.getValue(), ec);
        // TODO Validate result type vs. desired type
        return Argument.builder().name(argumentMatch.getName()).
                value(evaluatedValue).build();
    }
}
