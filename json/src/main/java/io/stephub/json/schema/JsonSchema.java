package io.stephub.json.schema;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.stephub.json.*;
import io.stephub.json.jackson.JsonSchemaDeserializer;
import io.stephub.json.jackson.JsonSchemaSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@JsonSerialize(using = JsonSchemaSerializer.class)
@JsonDeserialize(using = JsonSchemaDeserializer.class)
@Slf4j
public class JsonSchema extends JsonObject {

    public static JsonSchema ofType(final JsonType type) {
        final JsonSchema schema = new JsonSchema();
        schema.setType(type);
        return schema;
    }

    public Json convertFrom(final Json input) {
        final List<JsonType> types = this.getTypes();
        for (final JsonType type : types) {
            try {
                return type.convertFrom(input);
            } catch (final JsonException e) {
                log.debug("Failed to convert input={} to type={}", input, type);
            }
        }
        throw new JsonException("Can't convert input to types: " + types.stream().map(t -> t.toString()).collect(Collectors.joining(", ")));
    }

    private List<JsonType> getTypes() {
        final Json typeRaw = this.getFields().get("type");
        if (typeRaw != null) {
            if (typeRaw instanceof JsonString) {
                return Collections.singletonList(this.getTypeFromJson(typeRaw));
            } else if (typeRaw instanceof JsonArray) {
                final List<JsonType> types = new ArrayList<>();
                for (final Json type : ((JsonArray) typeRaw).getValues()) {
                    types.add(this.getTypeFromJson(typeRaw));
                }
                if (types.isEmpty()) {
                    types.add(JsonType.JSON);
                }
                return types;
            } else {
                throw new JsonException("JSON type must be string or array of strings, but got: " + typeRaw);
            }
        }
        return Collections.singletonList(JsonType.JSON);
    }

    private JsonType getTypeFromJson(final Json typeRaw) {
        if (typeRaw instanceof JsonString) {
            try {
                return JsonType.valueOf(((JsonString) typeRaw).getValue().toUpperCase());
            } catch (final IllegalArgumentException e) {
                throw new JsonException("Unknown JSON type: " + typeRaw);
            }
        } else {
            throw new JsonException("JSON type value must be string, got: " + typeRaw);
        }
    }

    public void setType(final JsonType type) {
        this.getFields().put("type", new JsonString(type.toString()));
    }
}
