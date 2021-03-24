package io.stephub.json;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@AllArgsConstructor
@Getter
public class JsonNumber extends Json {
    private final Number value;

    @Override
    public String asJsonString(final boolean pretty) {
        return this.value.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JsonNumber)) {
            return false;
        }
        final JsonNumber that = (JsonNumber) o;
        return Objects.equals(this.value.toString(), that.value.toString());
    }

    @Override
    public int hashCode() {
        return this.value.toString().hashCode();
    }
}
