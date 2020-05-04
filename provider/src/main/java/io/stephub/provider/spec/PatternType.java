package io.stephub.provider.spec;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PatternType {
    REGEX,
    SIMPLE;

    @Override
    @JsonValue
    public String toString() {
        return this.name().toLowerCase();
    }
}
