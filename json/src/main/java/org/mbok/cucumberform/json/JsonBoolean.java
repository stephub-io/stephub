package org.mbok.cucumberform.json;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class JsonBoolean extends Json {
    private boolean value;

    @Override
    public String asJsonString(boolean pretty) {
        return Boolean.toString(value);
    }

    @Override
    public JsonType getType() {
        return JsonType.BOOLEAN;
    }
}
