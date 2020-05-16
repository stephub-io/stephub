package io.stephub.expression.grammar;

import io.stephub.expression.ParseException;
import io.stephub.expression.generated.ExpressionsBaseVisitor;
import io.stephub.expression.generated.ExpressionsParser;
import io.stephub.expression.model.*;
import io.stephub.json.Json;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class JsonVisitor extends ExpressionsBaseVisitor<JsonValueNode<?>> {
    private final StringInterpolator stringInterpolater = new StringInterpolator();

    @Override
    public JsonValueNode<?> visitJson(final ExpressionsParser.JsonContext ctx) {
        return ctx.value().accept(this);
    }

    @Override
    public JsonObjectNode visitObj(final ExpressionsParser.ObjContext ctx) {
        final Map<JsonValueNode<? extends Json>, JsonValueNode<? extends Json>> fields = new HashMap<>();
        for (final ExpressionsParser.PairContext pair : ctx.pair()) {
            final JsonValueNode<?> value = pair.value().accept(new JsonVisitor());
            fields.put(
                    this.mapString(pair.STRING()),
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
            return new PathIndexNode(ctx.getText(), this.mapString(ctx.STRING()));
        } else if (ctx.INT_NUMBER() != null) {
            return new PathIndexNode(ctx.getText(), this.mapNumber(ctx.INT_NUMBER()));
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
            return this.mapString(ctx.STRING());
        } else if (ctx.NULL() != null) {
            return this.mapNull(ctx.NULL());
        } else if (ctx.BOOLEAN() != null) {
            return this.mapBoolean(ctx.BOOLEAN());
        } else if (ctx.NUMBER() != null) {
            return this.mapNumber(ctx.NUMBER());
        } else if (ctx.INT_NUMBER() != null) {
            return this.mapNumber(ctx.INT_NUMBER());
        }
        return super.visitValue(ctx);
        // throw new ParseException("Invalid JSON value type: " + ctx.getText());
    }

    @Override
    public JsonValueNode<?> visitFunction(final ExpressionsParser.FunctionContext ctx) {
        return new FunctionNode(ctx.ID().getText(),
                ctx.arguments() != null ? ctx.arguments().value().stream().
                        map(a -> a.accept(this)).collect(Collectors.toList()) :
                        Collections.emptyList());
    }

    private JsonBooleanNode mapBoolean(final TerminalNode tn) {
        return new JsonBooleanNode("true".equals(tn.getText()));
    }

    private JsonValueNode<? extends Json> mapString(final TerminalNode tn) {
        final String textIncludingQuotes = tn.getText();
        return this.stringInterpolater.interpolate(textIncludingQuotes.substring(1, textIncludingQuotes.length() - 1));
    }

    private JsonNullNode mapNull(final TerminalNode tn) {
        return new JsonNullNode();
    }

    private JsonNumberNode mapNumber(final TerminalNode tn) {
        final String nbText = tn.getText();
        if (nbText.contains(".")) {
            return new JsonNumberNode(Double.parseDouble(nbText));
        } else {
            final long l = Long.parseLong(nbText);
            if (l < Integer.MAX_VALUE) {
                return new JsonNumberNode((int) l);
            } else {
                return new JsonNumberNode(l);
            }
        }
    }
}
