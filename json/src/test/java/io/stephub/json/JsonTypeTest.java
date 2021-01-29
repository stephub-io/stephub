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
                BOOLEAN.convertFrom(JsonNull.INSTANCE),
                equalTo(JsonNull.INSTANCE)
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
        final JsonObject obj = new JsonObject();
        assertThat(
                OBJECT.convertFrom(obj),
                equalTo(obj)
        );
        assertThat(
                OBJECT.convertFrom(JsonNull.INSTANCE),
                equalTo(JsonNull.INSTANCE)
        );
        this.verifyMappingException(OBJECT, TRUE);
        this.verifyMappingException(OBJECT, new JsonArray());
        this.verifyMappingException(OBJECT, new JsonString(""));
        this.verifyMappingException(OBJECT, new JsonNumber(1));
    }

    @Test
    public void testToArrayMapping() {
        final JsonArray arr = new JsonArray();
        assertThat(
                ARRAY.convertFrom(arr),
                equalTo(arr)
        );
        assertThat(
                ARRAY.convertFrom(JsonNull.INSTANCE),
                equalTo(JsonNull.INSTANCE)
        );

        this.verifyMappingException(ARRAY, TRUE);
        this.verifyMappingException(ARRAY, new JsonObject());
        this.verifyMappingException(ARRAY, new JsonString(""));
        this.verifyMappingException(ARRAY, new JsonNumber(1));
    }

    private void verifyMappingException(final Json.JsonType targetType, final Json input) {
        final JsonException e = assertThrows(JsonException.class, () -> {
            targetType.convertFrom(input);
        });
        log.debug("Exception output", e);
    }
}