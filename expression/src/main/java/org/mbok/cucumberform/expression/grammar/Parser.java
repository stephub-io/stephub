package org.mbok.cucumberform.expression.grammar;

import org.antlr.v4.runtime.*;
import org.mbok.cucumberform.expression.generated.*;
import org.mbok.cucumberform.expression.model.ExprNode;

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
