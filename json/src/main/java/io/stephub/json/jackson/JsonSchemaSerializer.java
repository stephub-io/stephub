package io.stephub.json.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.stephub.json.schema.JsonSchema;

import java.io.IOException;

import static io.stephub.json.Json.JsonType.OBJECT;

public class JsonSchemaSerializer extends JacksonSerializer<JsonSchema> {
    public JsonSchemaSerializer() {
        super();
    }

    public JsonSchemaSerializer(final Class<JsonSchema> t) {
        super(t);
    }

    @Override
    public void serialize(final JsonSchema json, final JsonGenerator jsonGenerator, final SerializerProvider serializerProvider) throws IOException {
        this.serializeJson(OBJECT, json, jsonGenerator, serializerProvider);
    }
}
