package org.mbok.cucumberform.json;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class JsonString extends Json {
    private final String text;

    @Override
    public String asString() {
        return "\"" + text.replaceAll("\"", "\\\"") + "\"";
    }
}
