package io.stephub.expression.impl;

import io.stephub.expression.EvaluationException;
import io.stephub.expression.ExpressionEvaluator;
import io.stephub.expression.MatchResult;
import io.stephub.json.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
public class PathEvaluatorTest {
    private final ExpressionEvaluator el = new DefaultExpressionEvaluator();

    @Test
    public void testValidRefPathL1() {
        final Json result = this.el.evaluate("${abc}",
                SimpleEvaluationContext.builder().attribute("abc", JsonBoolean.TRUE).build());
        assertEquals(JsonBoolean.TRUE, result);
    }

    @Test
    public void testRefPathL1Assignment() {
        final MatchResult match = this.el.match("${abc}");
        final SimpleEvaluationContext ec = new SimpleEvaluationContext(new HashMap<>(), new HashMap<>());
        this.el.assign(match.getCompiledExpression(), ec, JsonBoolean.TRUE);
        assertEquals(JsonBoolean.TRUE, ec.get("abc"));
    }

    @Test
    public void testConvenientRefConcatenationInString() {
        final Json result = this.el.evaluate("\"text ${abc}\"",
                SimpleEvaluationContext.builder().attribute("abc", new JsonString("plus")).build());
        assertEquals(new JsonString("text plus"), result);
    }

    @Test
    public void testValidRefPathNull() {
        final Json result = this.el.evaluate("${abc}",
                SimpleEvaluationContext.builder().build());
        assertEquals(JsonNull.INSTANCE, result);
    }

    @Test
    public void testValidRefPathL2() {
        final Json result = this.el.evaluate("${abc.def}",
                SimpleEvaluationContext.builder().attribute("abc",
                        JsonObject.builder().field("def", JsonBoolean.TRUE).build()
                ).build());
        assertEquals(JsonBoolean.TRUE, result);
    }


    @Test
    public void testInvalidRefPathInBoolean() {
        assertThrows(EvaluationException.class, () -> {
            this.el.evaluate("${abc.def}",
                    SimpleEvaluationContext.builder().attribute("abc", JsonBoolean.TRUE).build());
        });
    }

    @Test
    public void testInvalidRefPathInNull() {
        assertThrows(EvaluationException.class, () -> {
            this.el.evaluate("${abc.def}",
                    SimpleEvaluationContext.builder().build());
        });
    }

    @Test
    public void testValidRefPathStringIndexL1() {
        final Json result = this.el.evaluate("${abc[\"def\"]}",
                SimpleEvaluationContext.builder().attribute("abc",
                        JsonObject.builder().field("def", JsonBoolean.TRUE).build()
                ).build());
        assertEquals(JsonBoolean.TRUE, result);
    }

    @Test
    public void testValidRefPathStringIndexL2() {
        final Json result = this.el.evaluate("${abc[\"def\"][\"hij\"]}",
                SimpleEvaluationContext.builder().attribute("abc",
                        JsonObject.builder().field("def",
                                JsonObject.builder().field("hij", JsonBoolean.TRUE).build()).build()
                ).build());
        assertEquals(JsonBoolean.TRUE, result);
    }

    @Test
    public void testValidRefPathStringIndexPath() {
        final Json result = this.el.evaluate("${abc[\"def\"].hij}",
                SimpleEvaluationContext.builder().attribute("abc",
                        JsonObject.builder().field("def",
                                JsonObject.builder().field("hij", JsonBoolean.TRUE).build()).build()
                ).build());
        assertEquals(JsonBoolean.TRUE, result);
    }

    @Test
    public void testValidRefPathStringIndexPathIndex() {
        final Json result = this.el.evaluate("${abc[\"def\"].hij[\"klm\"]}",
                SimpleEvaluationContext.builder().attribute("abc",
                        JsonObject.builder().field("def",
                                JsonObject.builder().field("hij",
                                        JsonObject.builder().field("klm", JsonBoolean.TRUE).build()).
                                        build()).build()
                ).build());
        assertEquals(JsonBoolean.TRUE, result);
    }

    @Test
    public void testValidRefPathStringIndexL2Null() {
        final Json result = this.el.evaluate("${abc[\"def\"][\"hij\"]}",
                SimpleEvaluationContext.builder().attribute("abc",
                        JsonObject.builder().field("def",
                                JsonObject.builder().build()).build()
                ).build());
        assertEquals(JsonNull.INSTANCE, result);
    }

    @Test
    public void testInvalidRefPathStringIndexInNull() {
        final Throwable exception = assertThrows(EvaluationException.class, () -> {
            this.el.evaluate("${abc[\"def\"]}",
                    SimpleEvaluationContext.builder().build());
        });
        assertEquals("Invalid index in reference 'abc[\"def\"]' to evaluate in JSON of type 'null'", exception.getMessage());
    }

    @Test
    public void testValidRefPathIndirectStringIndex() {
        final Json result = this.el.evaluate("${abc[indirectVar[\"value\"]][\"hij\"]}",
                SimpleEvaluationContext.builder().
                        attribute("abc",
                                JsonObject.builder().
                                        field("def",
                                                JsonObject.builder().field("hij", JsonBoolean.TRUE).build()
                                        )
                                        .build()
                        ).
                        attribute("indirectVar",
                                JsonObject.builder().field("value", new JsonString("def")).build()).
                        build()
        );
        assertEquals(JsonBoolean.TRUE, result);
    }

    @Test
    public void testValidRefPathArrayIndexL1() {
        final Json result = this.el.evaluate("${abc[0]}",
                SimpleEvaluationContext.builder().attribute("abc",
                        JsonArray.builder().value(JsonBoolean.TRUE).build()
                ).build());
        assertEquals(JsonBoolean.TRUE, result);
    }

    @Test
    public void testInvalidRefPathArrayIndexAsString() {
        final Throwable exception = assertThrows(EvaluationException.class, () -> {
            final Json result = this.el.evaluate("${abc[\"0\"]}",
                    SimpleEvaluationContext.builder().attribute("abc",
                            JsonArray.builder().value(JsonBoolean.TRUE).build()
                    ).build());
        });
        assertEquals("Invalid index 'string' in reference 'abc[\"0\"]' to evaluate in JSON of type 'array'", exception.getMessage());
    }

    @Test
    public void testValidRefPathIndirectArrayIndex() {
        final Json result = this.el.evaluate("${abc[def]}",
                SimpleEvaluationContext.builder().
                        attribute("abc",
                                JsonArray.builder().value(JsonBoolean.TRUE).build()
                        ).
                        attribute("def", new JsonNumber(0)).build());
        assertEquals(JsonBoolean.TRUE, result);
    }
}