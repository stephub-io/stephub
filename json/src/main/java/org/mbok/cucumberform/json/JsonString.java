package org.mbok.cucumberform.json;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class JsonString extends Json {
    private final String value;

    @Override
    public String asJsonString(boolean pretty) {
        return "\"" + encodeString(value) + "\"";
    }

    @Override
    public JsonType getType() {
        return JsonType.STRING;
    }
}
