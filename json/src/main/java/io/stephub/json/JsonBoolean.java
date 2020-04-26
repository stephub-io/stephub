package io.stephub.json;

public class JsonBoolean extends Json {
    public static final JsonBoolean TRUE = new JsonBoolean();
    public static final JsonBoolean FALSE = new JsonBoolean();

    private JsonBoolean() {
        super();
    }

    public static JsonBoolean valueOf(final boolean value) {
        return value ? TRUE : FALSE;
    }

    @Override
    public String asJsonString(final boolean pretty) {
        return Boolean.toString(this.isTrue());
    }

    public final boolean isTrue() {
        return this == TRUE;
    }

    @Override
    public int hashCode() {
        return this.isTrue() ? 1 : -1;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj == this;
    }
}
