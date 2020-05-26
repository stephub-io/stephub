package io.stephub.expression;

import io.stephub.json.Json;

public interface AttributesContext {
    Json get(String key);

    void put(String key, Json value);
}
