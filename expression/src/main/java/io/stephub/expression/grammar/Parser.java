package io.stephub.expression.grammar;

import io.stephub.expression.generated.ExpressionsLexer;
import io.stephub.expression.generated.ExpressionsParser;
import io.stephub.expression.model.ExprNode;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;

public class Parser {
    private static final SyntaxErrorListener ERROR_LISTENER = new SyntaxErrorListener();

    public ExprNode parse(final String input) {
        final ExpressionsLexer lexer = new ExpressionsLexer(CharStreams.fromString(input));
        lexer.removeErrorListeners();
        lexer.addErrorListener(ERROR_LISTENER);
        final TokenStream tokens = new CommonTokenStream(lexer);
        final ExpressionsParser parser = new ExpressionsParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(ERROR_LISTENER);
        final ExprVisitor exprVisitor = new ExprVisitor();
        final ExprNode result = exprVisitor.visit(parser.expr());
        return result;
    }
}
