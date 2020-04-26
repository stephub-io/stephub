package io.stephub.json.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.stephub.json.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class JacksonSerializerDeserializerTest {
    private final ObjectMapper mapper = new ObjectMapper();

    private String verifySerializationDeserialization(final Json given) throws JsonProcessingException {
        final String serialized = this.mapper.writeValueAsString(given);
        log.debug("Serialized input={} to output={}", given, serialized);
        assertEquals(given.asJsonString(false), serialized);
        final Json actual = this.mapper.readValue(serialized, Json.class);
        assertEquals(given, actual);
        return serialized;
    }

    @Test
    public void testSerializeNull() throws JsonProcessingException {
        this.verifySerializationDeserialization(JsonNull.INSTANCE);
    }

    @Test
    public void testSerializeString() throws JsonProcessingException {
        this.verifySerializationDeserialization(new JsonString("abc"));
    }

    @Test
    public void testSerializeNumberInt() throws JsonProcessingException {
        this.verifySerializationDeserialization(new JsonNumber(Integer.valueOf(109)));
    }

    @Test
    public void testSerializeNumberDouble() throws JsonProcessingException {
        this.verifySerializationDeserialization(new JsonNumber(Double.valueOf(109.999d)));
    }

    @Test
    public void testSerializeBooleanTrue() throws JsonProcessingException {
        this.verifySerializationDeserialization(JsonBoolean.TRUE);
    }

    @Test
    public void testSerializeBooleanFalse() throws JsonProcessingException {
        this.verifySerializationDeserialization(JsonBoolean.FALSE);
    }

    @Test
    public void testSerializeArray() throws JsonProcessingException {
        this.verifySerializationDeserialization(
                JsonArray.builder().value(JsonBoolean.TRUE).value(JsonBoolean.FALSE).build()
        );
    }

    @Test
    public void testSerializeObject() throws JsonProcessingException {
        this.verifySerializationDeserialization(
                JsonObject.builder().field("a", JsonBoolean.TRUE).
                        field("b", new JsonString("text")).build()
        );
    }
}