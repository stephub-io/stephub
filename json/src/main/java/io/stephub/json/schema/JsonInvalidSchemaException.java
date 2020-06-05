package io.stephub.json.schema;

public class JsonInvalidSchemaException extends RuntimeException {
    public JsonInvalidSchemaException(String message) {
        super(message);
    }

    public JsonInvalidSchemaException(String message, Throwable cause) {
        super(message, cause);
    }
}
