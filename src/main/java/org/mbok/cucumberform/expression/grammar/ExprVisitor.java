package org.mbok.cucumberform.expression.grammar;

import org.mbok.cucumberform.expression.generated.ExpressionsBaseVisitor;
import org.mbok.cucumberform.expression.generated.ExpressionsParser;
import org.mbok.cucumberform.expression.model.ExprNode;

public class ExprVisitor extends ExpressionsBaseVisitor<ExprNode> {
    @Override
    public ExprNode visitExpr(final ExpressionsParser.ExprContext ctx) {
        final ExprNode.ExprNodeBuilder builder = ExprNode.builder();
        builder.json(ctx.json().accept(new JsonVisitor()));
        return builder.build();
    }
}
