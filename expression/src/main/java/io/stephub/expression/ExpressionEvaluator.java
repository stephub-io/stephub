package io.stephub.expression;

import io.stephub.json.Json;

public interface ExpressionEvaluator {
    Json evaluate(String expression, EvaluationContext ec);
}
