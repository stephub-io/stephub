package org.mbok.cucumberform.runtime.service;

import org.mbok.cucumberform.expression.AttributesContext;
import org.mbok.cucumberform.expression.EvaluationContext;
import org.mbok.cucumberform.expression.ExpressionEvaluator;
import org.mbok.cucumberform.expression.impl.DefaultExpressionEvaluator;
import org.mbok.cucumberform.json.Json;
import org.mbok.cucumberform.provider.Argument;
import org.mbok.cucumberform.provider.StepRequest;
import org.mbok.cucumberform.provider.spec.StepSpec;
import org.springframework.stereotype.Service;

@Service
public class StepRequestEvaluator {
    private ExpressionEvaluator evaluator = new DefaultExpressionEvaluator();

    public void populateRequest(PatternMatcher.StepMatch stepMatch, StepRequest.StepRequestBuilder stepRequestBuilder, AttributesContext attributesContext) {
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

    private Argument evaluateArgument(EvaluationContext ec, PatternMatcher.ArgumentMatch argumentMatch) {
        Json evaluatedValue = evaluator.evaluate(argumentMatch.getValue(), ec);
        // TODO Validate result type vs. desired type
        return Argument.builder().name(argumentMatch.getName()).
                value(evaluatedValue).build();
    }
}
