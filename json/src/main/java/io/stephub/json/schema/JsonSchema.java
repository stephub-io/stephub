package io.stephub.json.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.networknt.schema.*;
import io.stephub.json.*;
import io.stephub.json.jackson.JsonSchemaDeserializer;
import io.stephub.json.jackson.JsonSchemaSerializer;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true, exclude = "rawSchema")
@SuperBuilder
@JsonSerialize(using = JsonSchemaSerializer.class)
@JsonDeserialize(using = JsonSchemaDeserializer.class)
@Slf4j
public class JsonSchema extends JsonObject {
    private static final String V201909_URI = "https://json-schema.org/draft/2019-09/schema";
    private static JsonSchemaFactory rawSchemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
    private static com.networknt.schema.JsonSchema schemaForSchema;

    static {
        final JsonMetaSchema metaSchema = JsonMetaSchema.builder(V201909_URI, JsonMetaSchema.getV201909()).
                addKeywords(Arrays.asList(new NonValidationKeyword("$vocabulary"), new NonValidationKeyword("$recursiveAnchor"), new NonValidationKeyword("$comment"), new NonValidationKeyword("propertyNames"), new NonValidationKeyword("$recursiveRef"))).build();
        final Map<String, String> metaSchemaUriMappings = new HashMap<>();
        metaSchemaUriMappings.put(V201909_URI,
                "classpath:/org/json-schema/draft/2019-09/schema");
        for (final String part : new String[]{"core", "applicator", "content", "format", "meta-data", "validation"}) {
            metaSchemaUriMappings.put("https://json-schema.org/draft/2019-09/meta/" + part,
                    "classpath:/org/json-schema/draft/2019-09/meta/" + part);
        }
        final JsonSchemaFactory metaSchemaFactory = new JsonSchemaFactory.Builder().defaultMetaSchemaURI(metaSchema.getUri())
                .addMetaSchema(metaSchema).addUriMappings(metaSchemaUriMappings)
                .build();
        try {
            schemaForSchema = metaSchemaFactory.getSchema(new URI(V201909_URI));
        } catch (final URISyntaxException e) {
            LoggerFactory.getLogger(JsonSchema.class).error("Init error", e);
        }
    }

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private com.networknt.schema.JsonSchema rawSchema;

    public static JsonSchema ofType(final JsonType type) {
        final JsonSchema schema = new JsonSchema();
        if (type != JsonType.ANY) {
            schema.setType(type);
        }
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

    public List<JsonType> getTypes() {
        final Json typeRaw = this.getFields().get("type");
        if (typeRaw != null) {
            if (typeRaw instanceof JsonString) {
                return Collections.singletonList(this.getTypeFromJson(typeRaw));
            } else if (typeRaw instanceof JsonArray) {
                final List<JsonType> types = new ArrayList<>();
                for (final Json type : ((JsonArray) typeRaw).getValues()) {
                    types.add(this.getTypeFromJson(type));
                }
                if (types.isEmpty()) {
                    types.add(JsonType.ANY);
                }
                return types;
            } else {
                throw new JsonException("JSON type must be string or array of strings, but got: " + typeRaw);
            }
        }
        return Collections.singletonList(JsonType.ANY);
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
        if (type != JsonType.ANY) {
            this.getFields().put("type", new JsonString(type.toString()));
        } else {
            this.getFields().remove("type");
        }
    }

    public void accept(final Json value) throws JsonInvalidSchemaException {
        Set<ValidationMessage> validationMessages = null;
        try {
            final com.networknt.schema.JsonSchema rawSchema = this.getRawSchema();
            validationMessages = rawSchema.validate(objectMapper.valueToTree(value));
        } catch (final Exception e) {
            throw new JsonInvalidSchemaException("Failed to validate schema due to: " + e.getMessage(), e);
        }
        if (!validationMessages.isEmpty()) {
            throw new JsonInvalidSchemaException(validationMessages.stream().map(Object::toString).collect(Collectors.joining("\n")));
        }
    }

    private com.networknt.schema.JsonSchema getRawSchema() {
        if (this.rawSchema == null) {
            this.rawSchema = rawSchemaFactory.getSchema(objectMapper.valueToTree(this));
        }
        return this.rawSchema;
    }

    public SchemaValidity validate() {
        try {
            final Set<ValidationMessage> validationMessages = schemaForSchema.
                    validate(objectMapper.valueToTree(this));
            if (validationMessages.isEmpty()) {
                return SchemaValidity.builder().valid(true).build();
            } else {
                return SchemaValidity.builder().valid(false).errors(
                        validationMessages.stream().map(vm -> vm.getMessage()).collect(Collectors.toList())
                ).build();
            }
        } catch (final Exception e) {
            log.debug("Error", e);
            return SchemaValidity.builder().valid(false).
                    error(e.getMessage()).
                    build();
        }
    }

    @Data
    @Builder
    public static class SchemaValidity {
        private boolean valid;
        @Singular
        private List<String> errors;
    }
}
