package io.stephub.expression.impl;

import io.stephub.expression.ExpressionEvaluator;
import io.stephub.expression.FunctionFactory.Function;
import io.stephub.json.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@Slf4j
public class FunctionEvaluatorTest {
    private final ExpressionEvaluator el = new DefaultExpressionEvaluator();

    @Test
    public void testCallZeroArgs() {
        // Given
        final Function func = mock(Function.class);
        when(func.invoke()).thenReturn(JsonBoolean.TRUE);

        // Call
        final Json result = this.el.evaluate("func1()",
                SimpleEvaluationContext.builder().
                        function("func1", func)
                        .build());

        // Expected
        assertEquals(JsonBoolean.TRUE, result);
        verify(func, times(1)).invoke();
    }

    @Test
    public void testCallZeroTwoArgs() {
        // Given
        final Function func = mock(Function.class);
        final JsonObject arg1 = JsonObject.builder().field("abc", JsonBoolean.TRUE).build();
        final JsonNumber arg2 = new JsonNumber(2);
        when(func.invoke(arg1, arg2)).thenReturn(null);

        // Call
        final Json result = this.el.evaluate("func1({\"abc\": true }, 2)",
                SimpleEvaluationContext.builder().
                        function("func1", func)
                        .build());

        // Expected
        assertEquals(JsonNull.INSTANCE, result);
        verify(func, times(1)).invoke(arg1, arg2);
    }
}