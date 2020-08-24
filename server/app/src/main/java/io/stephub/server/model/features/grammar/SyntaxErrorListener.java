package io.stephub.server.model.features.grammar;

import io.stephub.expression.ParseException;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

public class SyntaxErrorListener extends BaseErrorListener {
    private final String feature;

    public SyntaxErrorListener(final String feature) {
        this.feature = feature;
    }

    @Override
    public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol, final int line, final int charPositionInLine, final String msg, final RecognitionException e) {
        throw new ParseException("Failed to parse feature '" + this.feature + "' at line " + (line - 1) + ":" + charPositionInLine + " due to " + msg);
    }
}
