package io.stephub.expression.grammar;

import org.antlr.v4.runtime.*;
import io.stephub.expression.generated.*;
import io.stephub.expression.model.ExprNode;

public class Parser {
    public ExprNode parse(String input) {
        ExpressionsLexer lexer = new ExpressionsLexer(CharStreams.fromString(input));
        TokenStream tokens = new CommonTokenStream(lexer);
        ExpressionsParser parser = new ExpressionsParser(tokens);

        ExprVisitor exprVisitor = new ExprVisitor();
        ExprNode result = exprVisitor.visit(parser.expr());
        return result;
    }
}
