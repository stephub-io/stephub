package io.stephub.json;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class JsonString extends Json {
    private final String value;

    @Override
    public String asJsonString(final boolean pretty) {
        return "\"" + this.encodeString(this.value) + "\"";
    }

    @Override
    public String toString() {
        return this.value;
    }
}
