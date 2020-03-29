package org.mbok.cucumberform.expression.impl;

import org.junit.Test;
import org.mbok.cucumberform.expression.ExpressionEvaluator;
import org.mbok.cucumberform.json.Json;
import org.mbok.cucumberform.json.JsonBoolean;
import org.mbok.cucumberform.json.JsonNull;
import org.mbok.cucumberform.json.JsonString;

import static org.junit.Assert.assertEquals;

public class JsonExpressionEvaluatorTest {
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
}