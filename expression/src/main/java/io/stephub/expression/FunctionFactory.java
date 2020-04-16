package io.stephub.expression;

import io.stephub.json.Json;

public interface FunctionFactory {
    interface Function {
        Json invoke(Json... args) throws EvaluationException;
    }

    Function createFunction(String name);
}
