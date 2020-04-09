package org.mbok.cucumberform.expression.grammar;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.mbok.cucumberform.expression.ParseException;
import org.mbok.cucumberform.expression.generated.ExpressionsBaseVisitor;
import org.mbok.cucumberform.expression.generated.ExpressionsParser;
import org.mbok.cucumberform.expression.model.*;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class JsonVisitor extends ExpressionsBaseVisitor<JsonValueNode<?>> {
    @Override
    public JsonValueNode<?> visitJson(final ExpressionsParser.JsonContext ctx) {
        return ctx.value().accept(this);
    }

    @Override
    public JsonObjectNode visitObj(final ExpressionsParser.ObjContext ctx) {
        final Map<JsonStringNode, JsonValueNode<?>> fields = new HashMap<>();
        for (final ExpressionsParser.PairContext pair : ctx.pair()) {
            final JsonValueNode<?> value = pair.value().accept(new JsonVisitor());
            fields.put(
                    mapString(pair.STRING()),
                    value
            );
        }
        return new JsonObjectNode(fields);
    }

    @Override
    public ReferenceNode visitReference(final ExpressionsParser.ReferenceContext ctx) {
        return new ReferenceNode((PathNode) ctx.path().accept(new JsonVisitor()));
    }

    @Override
    public PathNode visitPath(final ExpressionsParser.PathContext ctx) {
        return new PathNode(ctx.ID().getText(),
                ctx.index().stream().map(i -> (PathIndexNode) i.accept(this)).collect(Collectors.toList()),
                ctx.path() == null ? null : (PathNode) ctx.path().accept(this));
    }

    @Override
    public PathIndexNode visitIndex(final ExpressionsParser.IndexContext ctx) {
        if (ctx.STRING() != null) {
            return new PathIndexNode(ctx.getText(), mapString(ctx.STRING()));
        } else if (ctx.INT_NUMBER() != null) {
            return new PathIndexNode(ctx.getText(), mapNumber(ctx.INT_NUMBER()));
        } else if (ctx.function() != null) {
            return new PathIndexNode(ctx.getText(), ctx.function().accept(this));
        } else if (ctx.path() != null) {
            return new PathIndexNode(ctx.getText(), ctx.path().accept(this));
        } else if (ctx.OPERATOR() != null) {
            // TODO
            return null;
        }
        throw new ParseException("Invalid index type: " + ctx.getText());
    }

    @Override
    public JsonValueNode<?> visitArr(final ExpressionsParser.ArrContext ctx) {
        return new JsonArrayNode(ctx.value().stream().
                map(i ->
                        i.accept(this)).
                collect(Collectors.toList()));
    }

    @Override
    public JsonValueNode<?> visitValue(final ExpressionsParser.ValueContext ctx) {
        if (ctx.STRING() != null) {
            return mapString(ctx.STRING());
        } else if (ctx.NULL() != null) {
            return mapNull(ctx.NULL());
        } else if (ctx.BOOLEAN() != null) {
            return mapBoolean(ctx.BOOLEAN());
        } else if (ctx.NUMBER() != null) {
            return mapNumber(ctx.NUMBER());
        } else if (ctx.INT_NUMBER() != null) {
            return mapNumber(ctx.INT_NUMBER());
        }
        return super.visitValue(ctx);
        // throw new ParseException("Invalid JSON value type: " + ctx.getText());
    }

    @Override
    public JsonValueNode<?> visitFunction(final ExpressionsParser.FunctionContext ctx) {
        return new FunctionNode(ctx.ID().getText(), ctx.value().stream().
                map(a -> a.accept(this)).collect(Collectors.toList()));
    }

    private static JsonBooleanNode mapBoolean(final TerminalNode tn) {
        return new JsonBooleanNode("true".equals(tn.getText()));
    }

    private static JsonStringNode mapString(final TerminalNode tn) {
        final String textIncludingQuotes = tn.getText();
        return new JsonStringNode(textIncludingQuotes.substring(1, textIncludingQuotes.length() - 1));
    }

    private static JsonNullNode mapNull(final TerminalNode tn) {
        return new JsonNullNode();
    }

    private static JsonNumberNode mapNumber(final TerminalNode tn) {
        try {
            return new JsonNumberNode(NumberFormat.getInstance().parse(tn.getText()));
        } catch (final java.text.ParseException e) {
            throw new ParseException(e.getMessage());
        }
    }

}
