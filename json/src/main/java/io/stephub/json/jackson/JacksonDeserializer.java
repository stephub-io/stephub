package io.stephub.json.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.stephub.json.*;

import java.io.IOException;

public class JacksonDeserializer extends StdDeserializer<Json> {
    public JacksonDeserializer() {
        this(null);
    }

    public JacksonDeserializer(final Class<?> vc) {
        super(vc);
    }

    @Override
    public Json deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        JsonToken token = jsonParser.currentToken();
        switch (token) {
            case VALUE_NULL:
                return JsonNull.INSTANCE;
            case VALUE_STRING:
                return new JsonString(jsonParser.getText());
            case VALUE_NUMBER_INT:
            case VALUE_NUMBER_FLOAT:
                return new JsonNumber(jsonParser.getNumberValue());
            case VALUE_TRUE:
                return JsonBoolean.TRUE;
            case VALUE_FALSE:
                return JsonBoolean.FALSE;
            case START_ARRAY:
                final JsonArray.JsonArrayBuilder arrayBuilder = JsonArray.builder();
                while ((token = jsonParser.nextToken()) != JsonToken.END_ARRAY) {
                    arrayBuilder.value(this.deserialize(jsonParser, deserializationContext));
                }
                return arrayBuilder.build();
            case START_OBJECT:
                final JsonObject.JsonObjectBuilder objBuilder = createObjBuilder();
                while ((token = jsonParser.nextToken()) != JsonToken.END_OBJECT) {
                    jsonParser.nextToken();
                    objBuilder.field(jsonParser.getCurrentName(), this.deserialize(jsonParser, deserializationContext));
                }
                return objBuilder.build();
        }
        throw new JsonException("Invalid JSON input at: " + jsonParser.getParsingContext().toString());
    }

    protected JsonObject.JsonObjectBuilder createObjBuilder() {
        return JsonObject.builder();
    }

    @Override
    public Json getNullValue(final DeserializationContext ctxt) throws JsonMappingException {
        return JsonNull.INSTANCE;
    }
}
