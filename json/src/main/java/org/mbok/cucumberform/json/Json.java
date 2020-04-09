package org.mbok.cucumberform.json;

public abstract class Json {
    public enum JsonType {
        OBJECT, ARRAY, BOOLEAN, STRING, NUMBER, NULL;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    public final String asJsonString() {
        return asJsonString(true);
    }

    public abstract String asJsonString(boolean pretty);

    @Override
    public String toString() {
        return asJsonString();
    }

    protected final String encodeString(String text) {
        return text.replaceAll("\"", "\\\"");
    }

    public abstract JsonType getType();
}
