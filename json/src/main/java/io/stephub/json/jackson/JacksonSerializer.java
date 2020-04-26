package io.stephub.json.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.stephub.json.*;

import java.io.IOException;

import static io.stephub.json.Json.JsonType.valueOf;

public class JacksonSerializer extends StdSerializer<Json> {

    public JacksonSerializer() {
        this(null);
    }

    protected JacksonSerializer(final Class<Json> t) {
        super(t);
    }

    @Override
    public void serialize(final Json json, final JsonGenerator jsonGenerator, final SerializerProvider serializerProvider) throws IOException {
        switch (valueOf(json)) {
            case STRING:
                serializerProvider.defaultSerializeValue(((JsonString) json).getValue(), jsonGenerator);
                break;
            case ARRAY:
                serializerProvider.defaultSerializeValue(((JsonArray) json).getValues(), jsonGenerator);
                break;
            case OBJECT:
                serializerProvider.defaultSerializeValue(((JsonObject) json).getFields(), jsonGenerator);
                break;
            case NULL:
                serializerProvider.defaultSerializeNull(jsonGenerator);
                break;
            case NUMBER:
                serializerProvider.defaultSerializeValue(((JsonNumber) json).getValue(), jsonGenerator);
                break;
            case BOOLEAN:
                serializerProvider.defaultSerializeValue(((JsonBoolean) json).isTrue(), jsonGenerator);
                break;
            default:
                throw new JsonException("Invalid JSON type: " + json);
        }
    }
}
