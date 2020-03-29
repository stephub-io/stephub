package org.mbok.cucumberform.expression.impl;

import org.mbok.cucumberform.expression.EvaluationContext;
import org.mbok.cucumberform.expression.ExpressionEvaluator;
import org.mbok.cucumberform.expression.grammar.Parser;
import org.mbok.cucumberform.expression.model.ExprNode;
import org.mbok.cucumberform.json.Json;

public class DefaultExpressionEvaluator implements ExpressionEvaluator {
    private Parser parser = new Parser();

    @Override
    public Json evaluate(String expression, EvaluationContext ec) {
        ExprNode expr = parser.parse(expression);
        return expr.evaluate(ec);
    }
}
