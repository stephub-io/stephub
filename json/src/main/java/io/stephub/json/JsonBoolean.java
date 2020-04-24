package io.stephub.json;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class JsonBoolean extends Json {
    private final boolean value;
    public static final JsonBoolean TRUE = new JsonBoolean(true);
    public static final JsonBoolean FALSE = new JsonBoolean(false);

    @Override
    public String asJsonString(final boolean pretty) {
        return Boolean.toString(this.value);
    }

}
