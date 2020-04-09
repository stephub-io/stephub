package org.mbok.cucumberform.expression.impl;

import lombok.Builder;
import lombok.Singular;
import org.mbok.cucumberform.expression.EvaluationContext;
import org.mbok.cucumberform.json.Json;

import java.util.Map;

@Builder
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
    public Function createFunction(final String name) {
        if (this.functions.containsKey(name)) {
            return (args -> this.functions.get(name).invoke(args));
        } else {
            return null;
        }
    }
}
