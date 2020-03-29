package org.mbok.cucumberform.expression;

import org.mbok.cucumberform.json.Json;

public interface ExpressionEvaluator {
    Json evaluate(String expression, EvaluationContext ec);
}
