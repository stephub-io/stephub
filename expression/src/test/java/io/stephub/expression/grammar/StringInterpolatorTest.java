package io.stephub.expression.grammar;

import io.stephub.expression.ParseException;
import io.stephub.expression.model.ExprNode;
import io.stephub.expression.model.JsonStringNode;
import io.stephub.expression.model.OpNode;
import io.stephub.expression.model.ReferenceNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.stephub.expression.model.OpNode.Operator.PLUS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StringInterpolatorTest {
    private Parser refParser;

    private StringInterpolator interpolator;

    @BeforeEach
    public void setUp() {
        this.refParser = mock(Parser.class);
        this.interpolator = new StringInterpolator(this.refParser);
    }

    @Test
    public void testEmptyString() {
        assertEquals(new JsonStringNode(""), this.interpolator.interpolate(""));
    }

    @Test
    public void testStringsOnly() {
        assertEquals(new JsonStringNode("abc def"), this.interpolator.interpolate("abc def"));
        assertEquals(new JsonStringNode("abc $def"), this.interpolator.interpolate("abc $def"));
    }

    @Test
    public void testEscapedRef() {
        assertEquals(new JsonStringNode("abc ${def}"), this.interpolator.interpolate("abc \\${def}"));
    }

    @Test
    public void testStringWithRef() {
        final ReferenceNode refNode = mock(ReferenceNode.class);
        when(this.refParser.parse("${ref}")).thenReturn(new ExprNode(refNode));
        assertEquals(
                new OpNode(PLUS,
                        new OpNode(PLUS, new JsonStringNode("abc "), refNode),
                        new JsonStringNode(" def")
                )
                , this.interpolator.interpolate("abc ${ref} def"));
    }

    @Test
    public void testComplexPath() {
        final ReferenceNode refNode = mock(ReferenceNode.class);
        when(this.refParser.parse("${dan[func1({ \"abc\": { \"def\": true, \"geh\": {} } })]}")).thenReturn(new ExprNode(refNode));
        assertEquals(
                new OpNode(PLUS,
                        new OpNode(PLUS, new JsonStringNode("test "), refNode),
                        new JsonStringNode(" $ok")
                )
                , this.interpolator.interpolate("test ${dan[func1({ \"abc\": { \"def\": true, \"geh\": {} } })]} $ok"));
    }

    @Test
    public void testInvalidPath() {
        assertThrows(ParseException.class, () -> {
            // Missing closing }
            this.interpolator.interpolate("test ${dan[func1({ \"abc\": { \"def\": true, \"geh\": {} })]} $ok");
        });
    }

    @Test
    public void testUnterminatedPath() {
        assertThrows(ParseException.class, () -> {
            this.interpolator.interpolate("test ${dan");
        });
    }
}