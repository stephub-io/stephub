package org.mbok.cucumberform.json;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class JsonNull extends Json {
    @Override
    public String asJsonString(boolean pretty) {
        return "null";
    }

    @Override
    public JsonType getType() {
        return JsonType.NULL;
    }
}
