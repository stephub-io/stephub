package io.stephub.json.schema;

import io.stephub.json.Json;
import io.stephub.json.JsonException;
import io.stephub.json.JsonObject;
import io.stephub.json.JsonString;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class JsonSchema extends JsonObject {

    public static JsonSchema ofType(final JsonType type) {
        final JsonSchema schema = new JsonSchema();
        schema.setType(type);
        return schema;
    }

    public JsonType getType() {
        final Json typeStr = this.getFields().get("type");
        if (typeStr != null) {
            if (typeStr instanceof JsonString) {
                try {
                    return JsonType.valueOf(((JsonString) typeStr).getValue().toUpperCase());
                } catch (final IllegalArgumentException e) {
                    throw new JsonException("Unknown JSON type: " + typeStr);
                }
            } else {
                throw new JsonException("JSON type must be string, got: " + typeStr);
            }
        }
        return JsonType.JSON;
    }

    public void setType(final JsonType type) {
        this.getFields().put("type", new JsonString(type.toString()));
    }
}
