package io.stephub.expression.grammar;

import io.stephub.expression.generated.ExpressionsBaseVisitor;
import io.stephub.expression.generated.ExpressionsParser;
import io.stephub.expression.model.ExprNode;

public class ExprVisitor extends ExpressionsBaseVisitor<ExprNode> {
    @Override
    public ExprNode visitExpr(final ExpressionsParser.ExprContext ctx) {
        final ExprNode.ExprNodeBuilder builder = ExprNode.builder();
        builder.json(ctx.json().accept(new JsonVisitor()));
        return builder.build();
    }
}
