package io.stephub.expression.grammar;

import io.stephub.expression.ParseException;
import io.stephub.expression.generated.StringInterpolation;
import io.stephub.expression.model.ExprNode;
import io.stephub.expression.model.JsonStringNode;
import io.stephub.expression.model.JsonValueNode;
import io.stephub.expression.model.OpNode;
import io.stephub.json.Json;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import static io.stephub.expression.model.OpNode.Operator.PLUS;

public class StringInterpolator {
    private final Parser referenceParser;

    public StringInterpolator() {
        this(new Parser());
    }

    StringInterpolator(final Parser referenceParser) {
        this.referenceParser = referenceParser;
    }

    public JsonValueNode<? extends Json> interpolate(final String input) {
        final StringInterpolation lexer = new StringInterpolation(CharStreams.fromString(input));
        final CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        tokenStream.fill();
        JsonValueNode<? extends Json> node = null;
        StringBuilder refBuilder = null;
        final StringBuilder trace = new StringBuilder();
        int openJsonObjects = 0;
        for (final Token t : tokenStream.getTokens()) {
            trace.append(t.getText());
            switch (t.getType()) {
                case (StringInterpolation.STRING_CONTENT):
                    if (refBuilder != null) {
                        throw new ParseException("Unterminated reference detected '" + refBuilder + "' in '" + input + "'");
                    }
                    final String str = t.getText().replaceAll("\\\\\\$\\{", "\\${");
                    if (node == null) {
                        node = new JsonStringNode(str);
                    } else {
                        node = new OpNode(PLUS, node, new JsonStringNode(str));
                    }
                    break;
                case (StringInterpolation.START_REF):
                    if (refBuilder != null) {
                        throw new ParseException("Unfinished reference '" + trace + "' in '" + input + "'");
                    }
                    refBuilder = new StringBuilder();
                    openJsonObjects--;
                case (StringInterpolation.JSON_OBJ_START):
                    openJsonObjects += 2; // Inc by 2 due to dec in next case
                case (StringInterpolation.JSON_OBJ_END):
                    openJsonObjects--;
                case (StringInterpolation.PATH):
                case (StringInterpolation.SOME_JSON):
                case (StringInterpolation.OBJ):
                    if (refBuilder == null) {
                        throw new ParseException("Illegal reference syntax '" + trace + "' in '" + input + "'");
                    }
                    refBuilder.append(t.getText());
                    break;
                case (StringInterpolation.END_REF):
                    if (refBuilder == null) {
                        throw new ParseException("Illegal reference syntax '" + trace + "' in '" + input + "'");
                    } else if (openJsonObjects != 0) {
                        throw new ParseException("Unexpected reference closure '" + refBuilder + "' in '" + input + "'");
                    }
                    refBuilder.append(t.getText());
                    final ExprNode refNode = this.referenceParser.parse(refBuilder.toString());
                    if (node == null) {
                        node = refNode.getJson();
                    } else {
                        node = new OpNode(PLUS, node, refNode.getJson());
                    }
                    refBuilder = null;
                    break;
            }
        }
        if (refBuilder != null) {
            throw new ParseException("Unterminated reference detected '" + refBuilder + "' in '" + input + "'");
        }
        if (node == null) {
            return new JsonStringNode("");
        } else {
            return node;
        }
    }
}
