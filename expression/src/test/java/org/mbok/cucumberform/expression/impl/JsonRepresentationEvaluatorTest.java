package org.mbok.cucumberform.expression.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mbok.cucumberform.expression.ExpressionEvaluator;
import org.mbok.cucumberform.json.*;

import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
public class JsonRepresentationEvaluatorTest {
    private final ExpressionEvaluator el = new DefaultExpressionEvaluator();

    @Test
    public void testJsonString() {
        final Json result = el.evaluate("\"test\"", null);
        assertEquals(new JsonString("test"), result);
    }

    @Test
    public void testJsonEscapedString() {
        final Json result = el.evaluate("\"test \\\"me\\\"\"", null);
        assertEquals(new JsonString("test \"me\""), result);
    }

    @Test
    public void testJsonNull() {
        final Json result = el.evaluate("null", null);
        assertEquals(new JsonNull(), result);
    }

    @Test
    public void testBooleanTrue() {
        final Json result = el.evaluate("true", null);
        assertEquals(new JsonBoolean(true), result);
    }

    @Test
    public void testBooleanFalse() {
        final Json result = el.evaluate("false", null);
        assertEquals(new JsonBoolean(false), result);
    }

    @Test
    public void testSimpleObject() {
        final Json result = el.evaluate("{ \"abc\": true}", null);
        log.debug("JSON pretty object: {}", result);
        log.debug("JSON non pretty object: {}", result.asJsonString(false));
        assertEquals(new JsonObject(singletonMap("abc", new JsonBoolean(true))), result);
    }

    @Test
    public void testTuppleObject() {
        final Json result = el.evaluate("{ \"abc\": true, \"def\": false }", null);
        log.debug("JSON pretty object: {}", result);
        log.debug("JSON non pretty object: {}", result.asJsonString(false));
        assertEquals(
                JsonObject.builder()
                        .field("abc", new JsonBoolean(true))
                        .field("def", new JsonBoolean(false))
                        .build(), result);
    }

    @Test
    public void testJsonIntNumber() {
        final Json result = el.evaluate("2", null);
        assertEquals(new JsonNumber(2), result);
    }

    @Test
    public void testSimpleArray() {
        final Json result = el.evaluate("[true, false]", null);
        log.debug("JSON pretty array: {}", result);
        log.debug("JSON non pretty array: {}", result.asJsonString(false));
        assertEquals(JsonArray.builder().value(new JsonBoolean(true)).value(new JsonBoolean(false)).build(), result);
    }
}