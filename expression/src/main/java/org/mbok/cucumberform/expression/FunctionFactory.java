package org.mbok.cucumberform.expression;

import org.mbok.cucumberform.json.Json;

import java.lang.reflect.Method;
import java.util.List;

public interface FunctionFactory {
    interface Function {
        Json invoke(Json... args) throws EvaluationException;
    }

    Function createFunction(String name);
}
