package io.stephub.json.jackson;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import io.stephub.json.Json;
import io.stephub.json.JsonObject;
import io.stephub.json.schema.JsonSchema;

public class JsonSchemaDeserializer extends JacksonDeserializer {
    @Override
    protected JsonObject.JsonObjectBuilder createObjBuilder() {
        return JsonSchema.builder();
    }

    @Override
    public Json getNullValue(final DeserializationContext ctxt) throws JsonMappingException {
        return JsonSchema.ofType(Json.JsonType.ANY);
    }
}
