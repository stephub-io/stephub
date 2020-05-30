package io.stephub.expression.impl;

import io.stephub.expression.EvaluationContext;
import io.stephub.json.Json;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;

import java.util.Map;

@Builder
@AllArgsConstructor
public class SimpleEvaluationContext implements EvaluationContext {
    @Singular
    private final Map<String, Json> attributes;

    @Singular
    private final Map<String, Function> functions;

    @Override
    public Json get(final String key) {
        return this.attributes.get(key);
    }

    @Override
    public void put(final String key, final Json value) {
        this.attributes.put(key, value);
    }

    @Override
    public Function createFunction(final String name) {
        if (this.functions.containsKey(name)) {
            return (args -> this.functions.get(name).invoke(args));
        } else {
            return null;
        }
    }
}
