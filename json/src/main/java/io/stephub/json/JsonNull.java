package io.stephub.json;

public class JsonNull extends Json {
    public static final JsonNull INSTANCE = new JsonNull();

    private JsonNull() {
        super();
    }

    @Override
    public String asJsonString(final boolean pretty) {
        return "null";
    }

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj == INSTANCE;
    }
}
