package io.stephub.expression.impl;

import io.stephub.expression.EvaluationException;
import io.stephub.expression.ExpressionEvaluator;
import io.stephub.expression.MatchResult;
import io.stephub.json.JsonArray;
import io.stephub.json.JsonBoolean;
import io.stephub.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
public class PathAssignmentTest {
    private final ExpressionEvaluator el = new DefaultExpressionEvaluator();

    @Test
    public void testRefPathL1Assignment() {
        final MatchResult match = this.el.match("${abc}");
        final SimpleEvaluationContext ec = new SimpleEvaluationContext(new HashMap<>(), new HashMap<>());
        assertThat(match.getCompiledExpression().isAssignable(), equalTo(true));
        this.el.assign(match.getCompiledExpression(), ec, JsonBoolean.TRUE);
        assertEquals(JsonBoolean.TRUE, ec.get("abc"));
    }


    @Test
    public void testInvalidAssignmentExpr() {
        final Throwable exception = assertThrows(EvaluationException.class, () -> {
            final MatchResult match = this.el.match("{}");
            assertThat(match.getCompiledExpression().isAssignable(), equalTo(false));
            this.el.assign(match.getCompiledExpression(), null, JsonBoolean.TRUE);
        });
        log.info("Error message: {}", exception.getMessage());
    }

    @Test
    public void testL2NullAssignment() {
        final SimpleEvaluationContext ec = new SimpleEvaluationContext(new HashMap<>(), new HashMap<>());
        final Throwable exception = assertThrows(EvaluationException.class, () -> {
            final MatchResult match = this.el.match("${abc.def}");
            assertThat(match.getCompiledExpression().isAssignable(), equalTo(true));
            this.el.assign(match.getCompiledExpression(), ec, JsonBoolean.TRUE);
        });
        log.info("Error message: {}", exception.getMessage());
    }

    @Test
    public void testL2Assignment() {
        final SimpleEvaluationContext ec = new SimpleEvaluationContext(Collections.singletonMap("abc", new JsonObject()), new HashMap<>());
        final MatchResult match = this.el.match("${abc.def}");
        assertThat(match.getCompiledExpression().isAssignable(), equalTo(true));
        this.el.assign(match.getCompiledExpression(), ec, JsonBoolean.TRUE);
        assertThat(((JsonObject) ec.get("abc")).getFields(), hasEntry("def", JsonBoolean.TRUE));
    }

    @Test
    public void testL2AssignmentOnIndex() {
        final SimpleEvaluationContext ec = new SimpleEvaluationContext(Collections.singletonMap("abc",
                JsonObject.builder().field("def",
                        JsonArray.builder().value(
                                new JsonObject(new HashMap<>(Collections.singletonMap("nested", JsonBoolean.FALSE)))
                        ).build()
                ).build()), new HashMap<>());
        final MatchResult match = this.el.match("${abc.def[0].nested}");
        assertThat(match.getCompiledExpression().isAssignable(), equalTo(true));
        this.el.assign(match.getCompiledExpression(), ec, JsonBoolean.TRUE);
        assertThat(ec.get("abc"), equalTo(
                JsonObject.builder().field("def",
                        JsonArray.builder().value(
                                JsonObject.builder().field("nested", JsonBoolean.TRUE).build()
                        ).build()
                ).build()
        ));
    }

    @Test
    public void testL2AssignmentOnNewIndex() {
        final SimpleEvaluationContext ec = new SimpleEvaluationContext(Collections.singletonMap("abc",
                JsonObject.builder().field("def",
                        new JsonArray(new ArrayList<>())
                ).build()), new HashMap<>());
        final MatchResult match = this.el.match("${abc.def[0]}");
        assertThat(match.getCompiledExpression().isAssignable(), equalTo(true));
        this.el.assign(match.getCompiledExpression(), ec, JsonBoolean.TRUE);
        assertThat(ec.get("abc"), equalTo(
                JsonObject.builder().field("def",
                        JsonArray.builder().value(
                                JsonBoolean.TRUE
                        ).build()
                ).build()
        ));
    }

    @Test
    public void testL2AssignmentOnDoubleIndex() {
        final SimpleEvaluationContext ec = new SimpleEvaluationContext(Collections.singletonMap("abc",
                JsonObject.builder().field("def",
                        JsonArray.builder().value(
                                JsonArray.builder()
                                        .value(JsonBoolean.FALSE)
                                        .value(
                                                new JsonObject(new HashMap<>(Collections.singletonMap("nested", JsonBoolean.FALSE)))
                                        ).build()
                        ).build()
                ).build()), new HashMap<>());
        final MatchResult match = this.el.match("${abc.def[0][1].nested}");
        assertThat(match.getCompiledExpression().isAssignable(), equalTo(true));
        this.el.assign(match.getCompiledExpression(), ec, JsonBoolean.TRUE);
        assertThat(ec.get("abc"), equalTo(
                JsonObject.builder().field("def",
                        JsonArray.builder().value(
                                JsonArray.builder()
                                        .value(JsonBoolean.FALSE)
                                        .value(
                                                JsonObject.builder().field("nested", JsonBoolean.TRUE).build()
                                        ).build()
                        ).build()
                ).build()
        ));
    }
}