package io.stephub.expression.impl;

import io.stephub.expression.*;
import io.stephub.expression.grammar.Parser;
import io.stephub.expression.model.AssignableNode;
import io.stephub.expression.model.ExprNode;
import io.stephub.json.Json;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

public class DefaultExpressionEvaluator implements ExpressionEvaluator {
    private final Parser parser = new Parser();

    @Override
    public Json evaluate(final String expression, final EvaluationContext ec) {
        final ExprNode expr = this.parser.parse(expression);
        return expr.evaluate(ec);
    }

    @Override
    public MatchResult match(final String expressionString) {
        final SimpleMatchResult.SimpleMatchResultBuilder resultBuilder = SimpleMatchResult.builder().
                original(expressionString);
        try {
            resultBuilder.expr(this.parser.parse(expressionString));
        } catch (final ParseException e) {
            resultBuilder.parseException(e);
        }
        return resultBuilder.build();
    }

    @Override
    public Json evaluate(final CompiledExpression compiledExpression, final EvaluationContext ec) {
        return ((SimpleCompiledExpression) compiledExpression).getExpr().evaluate(ec);
    }

    @Override
    public void assign(final CompiledExpression compiledExpression, final EvaluationContext ec, final Json value) {
        final ExprNode expr = ((SimpleCompiledExpression) compiledExpression).getExpr();
        if (expr.getJson() instanceof AssignableNode) {
            ((AssignableNode) expr.getJson()).assign(ec, value);
        } else {
            throw new EvaluationException("Not assignable expression '" + ((SimpleCompiledExpression) compiledExpression).getOriginal() + "'");
        }
    }

    @Builder
    @EqualsAndHashCode
    private static final class SimpleMatchResult implements MatchResult {
        private final ParseException parseException;
        private final ExprNode expr;
        private final String original;

        @Override
        public boolean matches() {
            return this.parseException == null;
        }

        @Override
        public ParseException getParseException() {
            return this.parseException;
        }

        @Override
        public CompiledExpression getCompiledExpression() {
            return new SimpleCompiledExpression(this.expr, this.original);
        }
    }

    @Getter
    @AllArgsConstructor
    @EqualsAndHashCode
    private static final class SimpleCompiledExpression implements CompiledExpression {
        private final ExprNode expr;
        private final String original;

        @Override
        public boolean isAssignable() {
            return this.expr.getJson() instanceof AssignableNode;
        }
    }
}
