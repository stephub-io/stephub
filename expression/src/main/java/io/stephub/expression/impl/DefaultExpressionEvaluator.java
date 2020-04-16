package io.stephub.expression.impl;

import io.stephub.expression.EvaluationContext;
import io.stephub.expression.ExpressionEvaluator;
import io.stephub.expression.grammar.Parser;
import io.stephub.expression.model.ExprNode;
import io.stephub.json.Json;

public class DefaultExpressionEvaluator implements ExpressionEvaluator {
    private Parser parser = new Parser();

    @Override
    public Json evaluate(String expression, EvaluationContext ec) {
        ExprNode expr = parser.parse(expression);
        return expr.evaluate(ec);
    }
}
