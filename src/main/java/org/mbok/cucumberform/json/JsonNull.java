package org.mbok.cucumberform.json;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class JsonNull extends Json {
    @Override
    public String asString() {
        return "null";
    }
}
