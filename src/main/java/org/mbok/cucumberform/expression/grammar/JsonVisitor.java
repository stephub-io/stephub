package org.mbok.cucumberform.expression.grammar;

import org.mbok.cucumberform.expression.generated.ExpressionsBaseVisitor;
import org.mbok.cucumberform.expression.generated.ExpressionsParser;
import org.mbok.cucumberform.expression.model.JsonBooleanNode;
import org.mbok.cucumberform.expression.model.JsonNullNode;
import org.mbok.cucumberform.expression.model.JsonStringNode;
import org.mbok.cucumberform.expression.model.JsonValueNode;

public class JsonVisitor extends ExpressionsBaseVisitor<JsonValueNode> {
    @Override
    public JsonValueNode visitJson(final ExpressionsParser.JsonContext ctx) {
        if (ctx.value().STRING() != null) {
            return new JsonStringNode(ctx.value().STRING().getText());
        } else if (ctx.value().NULL() != null) {
            return new JsonNullNode();
        } else if (ctx.value().BOOLEAN() != null) {
            return new JsonBooleanNode("true".equals(ctx.value().BOOLEAN().getText()));
        }
        return null;
    }
}
