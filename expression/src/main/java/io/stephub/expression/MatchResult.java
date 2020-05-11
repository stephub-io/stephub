package io.stephub.expression;

public interface MatchResult {
    boolean matches();

    ParseException getParseException();

    CompiledExpression getCompiledExpression();
}
