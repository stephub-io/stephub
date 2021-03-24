package io.stephub.json;

import lombok.Getter;

@Getter
public class JsonInteger extends JsonNumber {
    public JsonInteger(final Integer value) {
        super(value);
    }
}
