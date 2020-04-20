package io.stephub.json;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static io.stephub.json.Json.JsonType.*;
import static io.stephub.json.JsonBoolean.FALSE;
import static io.stephub.json.JsonBoolean.TRUE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
public class JsonTypeTest {
    @Test
    public void testToBooleanMapping() {
        assertThat(
                BOOLEAN.convertFrom(new JsonNumber(0.0001d)),
                equalTo(TRUE)
        );
        assertThat(
                BOOLEAN.convertFrom(new JsonNumber(0.000d)),
                equalTo(FALSE)
        );
        assertThat(
                BOOLEAN.convertFrom(new JsonNull()),
                equalTo(FALSE)
        );
        assertThat(
                BOOLEAN.convertFrom(TRUE),
                equalTo(TRUE)
        );
        // Empty array
        assertThat(
                BOOLEAN.convertFrom(new JsonArray()),
                equalTo(FALSE)
        );
        // Non empty array
        assertThat(
                BOOLEAN.convertFrom(JsonArray.builder().value(FALSE).build()),
                equalTo(TRUE)
        );
        // Empty object
        assertThat(
                BOOLEAN.convertFrom(new JsonObject()),
                equalTo(FALSE)
        );
        // Non empty object
        assertThat(
                BOOLEAN.convertFrom(JsonObject.builder().field("abc", FALSE).build()),
                equalTo(TRUE)
        );
        // Empty string
        assertThat(
                BOOLEAN.convertFrom(new JsonString("")),
                equalTo(FALSE)
        );
        // Non empty string
        assertThat(
                BOOLEAN.convertFrom(new JsonString(" ")),
                equalTo(TRUE)
        );
    }

    @Test
    public void testToObjectMapping() {
        JsonObject obj = new JsonObject();
        assertThat(
                OBJECT.convertFrom(obj),
                equalTo(obj)
        );

        verifyMappingException(OBJECT, new JsonNull());
        verifyMappingException(OBJECT, TRUE);
        verifyMappingException(OBJECT, new JsonArray());
        verifyMappingException(OBJECT, new JsonString(""));
        verifyMappingException(OBJECT, new JsonNumber(1));
    }

    @Test
    public void testToArrayMapping() {
        JsonArray arr = new JsonArray();
        assertThat(
                ARRAY.convertFrom(arr),
                equalTo(arr)
        );

        verifyMappingException(ARRAY, new JsonNull());
        verifyMappingException(ARRAY, TRUE);
        verifyMappingException(ARRAY, new JsonObject());
        verifyMappingException(ARRAY, new JsonString(""));
        verifyMappingException(ARRAY, new JsonNumber(1));
    }

    private void verifyMappingException(Json.JsonType targetType, Json input) {
        final JsonMappingException e = assertThrows(JsonMappingException.class, () -> {
           targetType.convertFrom(input);
        });
        log.debug("Exception output", e);
    }
}