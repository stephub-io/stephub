package io.stephub.json;

import java.text.NumberFormat;

public abstract class Json {
    public enum JsonType {
        OBJECT(JsonObject.class),
        ARRAY(JsonArray.class),
        BOOLEAN(JsonBoolean.class),
        STRING(JsonString.class),
        NUMBER(JsonNumber.class),
        NULL(JsonNull.class),
        JSON(Json.class);

        private final Class<? extends Json> desiredClass;

        JsonType(final Class<? extends Json> desiredClass) {
            this.desiredClass = desiredClass;
        }

        public static JsonType valueOf(final Class<?> type) {
            for (final JsonType jt : values()) {
                if (jt.desiredClass.equals(type)) {
                    return jt;
                }
            }
            throw new IllegalArgumentException("No JSON representation found for class=" + type.getName());
        }

        public Json convertFrom(final Json input) {
            final JsonType sourceType = input.getType();
            if (sourceType == this) {
                return input;
            }
            switch (this) {
                case NULL:
                case OBJECT:
                case ARRAY:
                    break;
                case STRING:
                    if (sourceType == BOOLEAN || sourceType == NUMBER) {
                        return new JsonString(input.toString());
                    }
                    break;
                case NUMBER:
                    if (sourceType == STRING) {
                        try {
                            return new JsonNumber(NumberFormat.getInstance().parse(((JsonString) input).getValue()));
                        } catch (final java.text.ParseException e) {
                            throw new JsonMappingException("Can't parse JSON string value '" + input + "' as JSON number");
                        }
                    }
                    break;
                case BOOLEAN:
                    if (sourceType == NULL) {
                        return new JsonBoolean(false);
                    } else if (sourceType == STRING) {
                        return new JsonBoolean(((JsonString) input).getValue().length() > 0);
                    } else if (sourceType == ARRAY) {
                        return new JsonBoolean(!((JsonArray) input).getValues().isEmpty());
                    } else if (sourceType == OBJECT) {
                        return new JsonBoolean(!((JsonObject) input).getFields().isEmpty());
                    } else if (sourceType == NUMBER) {
                        return new JsonBoolean(!((JsonNumber) input).getValue().toString().matches("[0\\.]+"));
                    }
                    break;
                case JSON:
                    return input;
            }
            throw this.getMappingException(input);
        }

        @Override
        public String toString() {
            return this.name().toLowerCase();
        }

        private JsonMappingException getMappingException(final Json input) {
            final JsonType sourceType = input.getType();
            return new JsonMappingException("Can't map JSON of type '" + sourceType + "' to type '" + this + "'");
        }
    }

    public final String asJsonString() {
        return this.asJsonString(true);
    }

    public abstract String asJsonString(boolean pretty);

    @Override
    public String toString() {
        return this.asJsonString(false);
    }

    protected final String encodeString(final String text) {
        return text.replaceAll("\"", "\\\"");
    }

    public abstract JsonType getType();
}
