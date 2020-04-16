package io.stephub.expression.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import io.stephub.expression.EvaluationException;
import io.stephub.expression.ExpressionEvaluator;
import io.stephub.json.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
public class PathEvaluatorTest {
    private final ExpressionEvaluator el = new DefaultExpressionEvaluator();

    @Test
    public void testValidRefPathL1() {
        final Json result = el.evaluate("${abc}",
                SimpleEvaluationContext.builder().attribute("abc", new JsonBoolean(true)).build());
        assertEquals(new JsonBoolean(true), result);
    }

    @Test
    public void testValidRefPathNull() {
        final Json result = el.evaluate("${abc}",
                SimpleEvaluationContext.builder().build());
        assertEquals(new JsonNull(), result);
    }

    @Test
    public void testValidRefPathL2() {
        final Json result = el.evaluate("${abc.def}",
                SimpleEvaluationContext.builder().attribute("abc",
                        JsonObject.builder().field("def", new JsonBoolean(true)).build()
                ).build());
        assertEquals(new JsonBoolean(true), result);
    }


    @Test
    public void testInvalidRefPathInBoolean() {
        assertThrows(EvaluationException.class, () -> {
            el.evaluate("${abc.def}",
                    SimpleEvaluationContext.builder().attribute("abc", new JsonBoolean(true)).build());
        });
    }

    @Test
    public void testInvalidRefPathInNull() {
        assertThrows(EvaluationException.class, () -> {
            el.evaluate("${abc.def}",
                    SimpleEvaluationContext.builder().build());
        });
    }

    @Test
    public void testValidRefPathStringIndexL1() {
        final Json result = el.evaluate("${abc[\"def\"]}",
                SimpleEvaluationContext.builder().attribute("abc",
                        JsonObject.builder().field("def", new JsonBoolean(true)).build()
                ).build());
        assertEquals(new JsonBoolean(true), result);
    }

    @Test
    public void testValidRefPathStringIndexL2() {
        final Json result = el.evaluate("${abc[\"def\"][\"hij\"]}",
                SimpleEvaluationContext.builder().attribute("abc",
                        JsonObject.builder().field("def",
                                JsonObject.builder().field("hij", new JsonBoolean(true)).build()).build()
                ).build());
        assertEquals(new JsonBoolean(true), result);
    }

    @Test
    public void testValidRefPathStringIndexPath() {
        final Json result = el.evaluate("${abc[\"def\"].hij}",
                SimpleEvaluationContext.builder().attribute("abc",
                        JsonObject.builder().field("def",
                                JsonObject.builder().field("hij", new JsonBoolean(true)).build()).build()
                ).build());
        assertEquals(new JsonBoolean(true), result);
    }

    @Test
    public void testValidRefPathStringIndexPathIndex() {
        final Json result = el.evaluate("${abc[\"def\"].hij[\"klm\"]}",
                SimpleEvaluationContext.builder().attribute("abc",
                        JsonObject.builder().field("def",
                                JsonObject.builder().field("hij",
                                        JsonObject.builder().field("klm", new JsonBoolean(true)).build()).
                                        build()).build()
                ).build());
        assertEquals(new JsonBoolean(true), result);
    }

    @Test
    public void testValidRefPathStringIndexL2Null() {
        final Json result = el.evaluate("${abc[\"def\"][\"hij\"]}",
                SimpleEvaluationContext.builder().attribute("abc",
                        JsonObject.builder().field("def",
                                JsonObject.builder().build()).build()
                ).build());
        assertEquals(new JsonNull(), result);
    }

    @Test
    public void testInvalidRefPathStringIndexInNull() {
        final Throwable exception = assertThrows(EvaluationException.class, () -> {
            el.evaluate("${abc[\"def\"]}",
                    SimpleEvaluationContext.builder().build());
        });
        assertEquals("Invalid index in reference 'abc[\"def\"]' to evaluate in JSON of type 'null'", exception.getMessage());
    }

    @Test
    public void testValidRefPathIndirectStringIndex() {
        final Json result = el.evaluate("${abc[indirectVar[\"value\"]][\"hij\"]}",
                SimpleEvaluationContext.builder().
                        attribute("abc",
                                JsonObject.builder().
                                        field("def",
                                                JsonObject.builder().field("hij", new JsonBoolean(true)).build()
                                        )
                                        .build()
                        ).
                        attribute("indirectVar",
                                JsonObject.builder().field("value", new JsonString("def")).build()).
                        build()
        );
        assertEquals(new JsonBoolean(true), result);
    }

    @Test
    public void testValidRefPathArrayIndexL1() {
        final Json result = el.evaluate("${abc[0]}",
                SimpleEvaluationContext.builder().attribute("abc",
                        JsonArray.builder().value(new JsonBoolean(true)).build()
                ).build());
        assertEquals(new JsonBoolean(true), result);
    }

    @Test
    public void testInvalidRefPathArrayIndexAsString() {
        final Throwable exception = assertThrows(EvaluationException.class, () -> {
        final Json result = el.evaluate("${abc[\"0\"]}",
                SimpleEvaluationContext.builder().attribute("abc",
                        JsonArray.builder().value(new JsonBoolean(true)).build()
                ).build());
        });
        assertEquals("Invalid index 'string' in reference 'abc[\"0\"]' to evaluate in JSON of type 'array'", exception.getMessage());
    }

    @Test
    public void testValidRefPathIndirectArrayIndex() {
        final Json result = el.evaluate("${abc[def]}",
                SimpleEvaluationContext.builder().
                    attribute("abc",
                        JsonArray.builder().value(new JsonBoolean(true)).build()
                    ).
                    attribute("def", new JsonNumber(0)).build());
        assertEquals(new JsonBoolean(true), result);
    }
}