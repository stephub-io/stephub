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
    public String asString() {
        return Boolean.toString(value);
    }
}
