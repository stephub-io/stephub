package io.stephub.json.jackson;

import io.stephub.json.JsonObject;
import io.stephub.json.schema.JsonSchema;

public class JsonSchemaDeserializer extends JacksonDeserializer {
    @Override
    protected JsonObject.JsonObjectBuilder createObjBuilder() {
        return JsonSchema.builder();
    }
}
