package io.stephub.expression.impl;

import io.stephub.expression.ExpressionEvaluator;
import io.stephub.expression.ParseException;
import io.stephub.json.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
public class JsonRepresentationEvaluatorTest {
    private final ExpressionEvaluator el = new DefaultExpressionEvaluator();


    @Test
    public void testInvalidJson() {
        final ParseException e = assertThrows(ParseException.class, () -> {
            this.el.evaluate("dfjkdfjkfd{dfdf]",
                    SimpleEvaluationContext.builder().build());
        });
        log.debug("Exception msg", e);
    }

    @Test
    public void testJsonString() {
        final Json result = this.el.evaluate("\"test\"", null);
        assertEquals(new JsonString("test"), result);
    }

    @Test
    public void testJsonEscapedString() {
        final Json result = this.el.evaluate("\"test \\\"me\\\"\"", null);
        assertEquals(new JsonString("test \"me\""), result);
    }

    @Test
    public void testJsonNull() {
        final Json result = this.el.evaluate("null", null);
        assertEquals(JsonNull.INSTANCE, result);
    }

    @Test
    public void testBooleanTrue() {
        final Json result = this.el.evaluate("true", null);
        assertEquals(new JsonBoolean(true), result);
    }

    @Test
    public void testBooleanFalse() {
        final Json result = this.el.evaluate("false", null);
        assertEquals(new JsonBoolean(false), result);
    }

    @Test
    public void testSimpleObject() {
        final Json result = this.el.evaluate("{ \"abc\": true}", null);
        log.debug("JSON pretty object: {}", result);
        log.debug("JSON non pretty object: {}", result.asJsonString(false));
        assertEquals(new JsonObject(singletonMap("abc", new JsonBoolean(true))), result);
    }

    @Test
    public void testSimpleObjectMultiline() {
        final Json result = this.el.evaluate("\n{ \n\t \"abc\"\n: \n true \n}\n", null);
        log.debug("JSON pretty object: {}", result);
        log.debug("JSON non pretty object: {}", result.asJsonString(false));
        assertEquals(new JsonObject(singletonMap("abc", new JsonBoolean(true))), result);
    }

    @Test
    public void testTuppleObject() {
        final Json result = this.el.evaluate("{ \"abc\": true, \"def\": false }", null);
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
        final Json result = this.el.evaluate("2", null);
        assertEquals(new JsonNumber(2), result);
    }

    @Test
    public void testSimpleArray() {
        final Json result = this.el.evaluate("[true, false]", null);
        log.debug("JSON pretty array: {}", result);
        log.debug("JSON non pretty array: {}", result.asJsonString(false));
        assertEquals(JsonArray.builder().value(new JsonBoolean(true)).value(new JsonBoolean(false)).build(), result);
    }
}