package io.stephub.json;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@AllArgsConstructor
@Getter
public class JsonNumber extends Json {
    private Number value;

    @Override
    public String asJsonString(boolean pretty) {
        return value.toString();
    }

    @Override
    public JsonType getType() {
        return JsonType.NUMBER;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonNumber that = (JsonNumber) o;
        return Objects.equals(value.toString(), that.value.toString());
    }

    @Override
    public int hashCode() {
        return value.toString().hashCode();
    }
}
