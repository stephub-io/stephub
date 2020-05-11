package io.stephub.expression;

import io.stephub.json.Json;

public interface ExpressionEvaluator {
    Json evaluate(String expression, EvaluationContext ec);

    /**
     * Try to match given string as expression.
     * @param expressionString
     * @return the match result
     */
    MatchResult match(String expressionString);

    Json evaluate(CompiledExpression compiledExpression, EvaluationContext ec);
}
