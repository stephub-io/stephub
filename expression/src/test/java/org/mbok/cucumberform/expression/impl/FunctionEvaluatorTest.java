package org.mbok.cucumberform.expression.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mbok.cucumberform.expression.EvaluationException;
import org.mbok.cucumberform.expression.ExpressionEvaluator;
import org.mbok.cucumberform.expression.FunctionFactory;
import org.mbok.cucumberform.expression.FunctionFactory.Function;
import org.mbok.cucumberform.json.*;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@Slf4j
public class FunctionEvaluatorTest {
    private final ExpressionEvaluator el = new DefaultExpressionEvaluator();

    @Test
    public void testCallZeroArgs() {
        // Given
        Function func = mock(Function.class);
        when(func.invoke()).thenReturn(new JsonBoolean(true));

        // Call
        final Json result = el.evaluate("func1()",
                SimpleEvaluationContext.builder().
                function("func1", func)
                .build());

        // Expected
        assertEquals(new JsonBoolean(true), result);
        verify(func, times(1)).invoke();
    }

    @Test
    public void testCallZeroTwoArgs() {
        // Given
        Function func = mock(Function.class);
        JsonObject arg1 =  JsonObject.builder().field("abc", new JsonBoolean(true)).build();
        JsonNumber arg2 = new JsonNumber(2);
        when(func.invoke(arg1, arg2)).thenReturn(null);

        // Call
        final Json result = el.evaluate("func1({\"abc\": true }, 2)",
                SimpleEvaluationContext.builder().
                        function("func1", func)
                        .build());

        // Expected
        assertEquals(new JsonNull(), result);
        verify(func, times(1)).invoke(arg1, arg2);
    }
}