package io.stephub.json;

public abstract class Json {
    public enum JsonType {
        OBJECT(JsonObject.class),
        ARRAY(JsonArray.class),
        BOOLEAN(JsonBoolean.class),
        STRING(JsonString.class),
        NUMBER(JsonNumber.class),
        NULL(JsonNull.class),
        JSON(Json.class);

        private Class<? extends Json> desiredClass;

        JsonType(Class<? extends Json> desiredClass) {
            this.desiredClass = desiredClass;
        }

        public static JsonType valueOf(Class<?> type) {
            for(JsonType jt:values()) {
                if (jt.desiredClass.equals(type)) {
                    return jt;
                }
            }
            throw new IllegalArgumentException("No JSON representation found for class="+type.getName());
        }

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
        return asJsonString(false);
    }

    protected final String encodeString(String text) {
        return text.replaceAll("\"", "\\\"");
    }

    public abstract JsonType getType();
}
