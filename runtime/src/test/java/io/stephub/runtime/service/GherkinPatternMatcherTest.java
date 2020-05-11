package io.stephub.runtime.service;

import io.stephub.expression.CompiledExpression;
import io.stephub.expression.ParseException;
import io.stephub.json.schema.JsonSchema;
import io.stephub.provider.api.model.spec.*;
import io.stephub.runtime.service.GherkinPatternMatcher.StepMatch;
import io.stephub.runtime.service.GherkinPatternMatcher.ValueMatch;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static io.stephub.json.Json.JsonType.BOOLEAN;
import static io.stephub.json.Json.JsonType.STRING;
import static io.stephub.json.schema.JsonSchema.ofType;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {GherkinPatternMatcher.class})
@Slf4j
public class GherkinPatternMatcherTest {
    @Autowired
    private GherkinPatternMatcher patternMatcher;

    @Test
    public void testDocString() {
        final StepSpec<JsonSchema> stepSpec = StepSpec.<JsonSchema>builder().pattern("Do with DocString payload").
                patternType(PatternType.REGEX).
                payload(StepSpec.PayloadType.DOC_STRING).
                docString(DocStringSpec.<JsonSchema>builder().schema(ofType(STRING)).build()).
                build();
        final StepMatch match = this.patternMatcher.matches(stepSpec,
                "Do with DocString payload\n" +
                        "  \"\"\"\n" +
                        "  My doc string line 1\n" +
                        "  My doc string line 2\n" +
                        "  \"\"\"");
        assertThat(match, notNullValue());
        assertThat(match.getDocString(), equalTo(
                ValueMatch.builder().desiredSchema(ofType(STRING)).value(
                        "My doc string line 1\n" +
                                "My doc string line 2").build()));
    }

    @Test
    public void testDocStringMissingOffset() {
        final StepSpec<JsonSchema> stepSpec = StepSpec.<JsonSchema>builder().pattern("Do with DocString payload").
                patternType(PatternType.REGEX).
                payload(StepSpec.PayloadType.DOC_STRING).
                docString(DocStringSpec.<JsonSchema>builder().schema(ofType(STRING)).build()).
                build();
        final StepMatch match = this.patternMatcher.matches(stepSpec,
                "Do with DocString payload\n" +
                        "  \"\"\"\n" +
                        "My doc string line 1\n" +
                        "My doc string line 2\n" +
                        "\"\"\"");
        assertThat(match, notNullValue());
        assertThat(match.getDocString(), equalTo(
                ValueMatch.builder().desiredSchema(ofType(STRING)).value(
                        "My doc string line 1\n" +
                                "My doc string line 2").build()));
    }

    @Test
    public void testDocMissingEndDelimiter() {
        final StepSpec<JsonSchema> stepSpec = StepSpec.<JsonSchema>builder().pattern("Do with DocString payload").
                patternType(PatternType.REGEX).
                payload(StepSpec.PayloadType.DOC_STRING).
                build();
        final ParseException e = assertThrows(ParseException.class, () -> {
            this.patternMatcher.matches(stepSpec,
                    "Do with DocString payload\n" +
                            "  \"\"\"\n" +
                            "My doc string line 1\n");
        });
        log.debug("Exception output", e);
    }

    @Test
    public void testMissingDocString() {
        final StepSpec<JsonSchema> stepSpec = StepSpec.<JsonSchema>builder().pattern("Do with DocString payload").
                patternType(PatternType.REGEX).
                payload(StepSpec.PayloadType.DOC_STRING).
                build();
        final ParseException e = assertThrows(ParseException.class, () -> {
            this.patternMatcher.matches(stepSpec,
                    "Do with DocString payload");
        });
        log.debug("Exception output", e);
    }

    @Test
    public void testMissingDocStringStartDelimiter() {
        final StepSpec<JsonSchema> stepSpec = StepSpec.<JsonSchema>builder().pattern("Do with DocString payload").
                patternType(PatternType.REGEX).
                payload(StepSpec.PayloadType.DOC_STRING).
                build();
        final ParseException e = assertThrows(ParseException.class, () -> {
            this.patternMatcher.matches(stepSpec,
                    "Do with DocString payload\n-Invalid delimiter");
        });
        log.debug("Exception output", e);
    }

    @Test
    public void testDataTableSingleCol() {
        final StepSpec<JsonSchema> stepSpec = StepSpec.<JsonSchema>builder().pattern("Do with DocString payload").
                patternType(PatternType.REGEX).
                payload(StepSpec.PayloadType.DATA_TABLE).
                dataTable(
                        DataTableSpec.<JsonSchema>builder().
                                header(false).
                                column(
                                        DataTableSpec.ColumnSpec.<JsonSchema>builder().
                                                name("condition").
                                                build()
                                ).
                                build()
                ).build();
        final StepMatch match = this.patternMatcher.matches(stepSpec,
                "Do with DocString payload\n" +
                        "| true |");
        assertThat(match, notNullValue());
        assertThat(match.getDataTable(), hasSize(1));
        assertThat(match.getDataTable().get(0), aMapWithSize(1));
        assertThat(match.getDataTable().get(0), hasEntry("condition",
                ValueMatch.builder().value("true").build()));
    }


    @Test
    public void testDataTableSingleColWithCommentAndEmptyLines() {
        final StepSpec<JsonSchema> stepSpec = StepSpec.<JsonSchema>builder().pattern("Do with DocString payload").
                patternType(PatternType.REGEX).
                payload(StepSpec.PayloadType.DATA_TABLE).
                dataTable(
                        DataTableSpec.<JsonSchema>builder().
                                header(false).
                                column(
                                        DataTableSpec.ColumnSpec.<JsonSchema>builder().
                                                name("condition").
                                                build()
                                ).
                                build()
                ).build();
        final StepMatch match = this.patternMatcher.matches(stepSpec,
                "  \n " +
                        "# Step with data table \n \n " +
                        " Do with DocString payload\n" +
                        "   # Some comment\n" +
                        "| true |\n" +
                        " | false | \n\n");
        assertThat(match, notNullValue());
        assertThat(match.getDataTable(), hasSize(2));
        assertThat(match.getDataTable().get(0), aMapWithSize(1));
        assertThat(match.getDataTable().get(0), hasEntry("condition",
                ValueMatch.builder().value("true").build()));
        assertThat(match.getDataTable().get(1), aMapWithSize(1));
        assertThat(match.getDataTable().get(1), hasEntry("condition",
                ValueMatch.builder().value("false").build()));
    }

    @Test
    public void testDataTableMultipleCols() {
        final StepSpec<JsonSchema> stepSpec = StepSpec.<JsonSchema>builder().pattern("Do with DocString payload").
                patternType(PatternType.REGEX).
                payload(StepSpec.PayloadType.DATA_TABLE).
                dataTable(
                        DataTableSpec.<JsonSchema>builder().
                                header(false).
                                column(
                                        DataTableSpec.ColumnSpec.<JsonSchema>builder().
                                                name("condition").
                                                build()
                                ).
                                column(
                                        DataTableSpec.ColumnSpec.<JsonSchema>builder().
                                                name("text").
                                                build()
                                ).
                                build()
                ).build();
        final StepMatch match = this.patternMatcher.matches(stepSpec,
                "Do with DocString payload\n" +
                        "   | true  | my text   | \n" +
                        "| false | next text | \n");
        assertThat(match, notNullValue());
        assertThat(match.getDataTable(), hasSize(2));
        assertThat(match.getDataTable().get(0), aMapWithSize(2));
        assertThat(match.getDataTable().get(0), hasEntry("condition",
                ValueMatch.builder().value("true").build()));
        assertThat(match.getDataTable().get(0), hasEntry("text",
                ValueMatch.builder().value("my text").build()));
        assertThat(match.getDataTable().get(1), aMapWithSize(2));
        assertThat(match.getDataTable().get(1), hasEntry("condition",
                ValueMatch.builder().value("false").build()));
        assertThat(match.getDataTable().get(1), hasEntry("text",
                ValueMatch.builder().value("next text").build()));
    }

    @Test
    public void testDataTableInvalidCols() {
        final StepSpec<JsonSchema> stepSpec = StepSpec.<JsonSchema>builder().pattern("Do with DocString payload").
                patternType(PatternType.REGEX).
                payload(StepSpec.PayloadType.DATA_TABLE).
                dataTable(
                        DataTableSpec.<JsonSchema>builder().
                                header(false).
                                column(
                                        DataTableSpec.ColumnSpec.<JsonSchema>builder().
                                                name("condition").
                                                build()
                                ).
                                column(
                                        DataTableSpec.ColumnSpec.<JsonSchema>builder().
                                                name("text").
                                                build()
                                ).
                                build()
                ).build();
        final ParseException e = assertThrows(ParseException.class, () -> {
            final StepMatch match = this.patternMatcher.matches(stepSpec,
                    "Do with DocString payload\n" +
                            "   | true  | my text   | \n" +
                            "| false | \n" +
                            " | false | abc |");
        });
        log.debug("Exception output", e);
    }


    @Test
    public void testDataTableSingleColWithHeader() {
        final StepSpec<JsonSchema> stepSpec = StepSpec.<JsonSchema>builder().pattern("Do with DocString payload").
                patternType(PatternType.REGEX).
                payload(StepSpec.PayloadType.DATA_TABLE).
                dataTable(
                        DataTableSpec.<JsonSchema>builder().
                                header(true).
                                column(
                                        DataTableSpec.ColumnSpec.<JsonSchema>builder().
                                                name("condition").
                                                build()
                                ).
                                build()
                ).build();
        final StepMatch match = this.patternMatcher.matches(stepSpec,
                "Do with DocString payload\n" +
                        " | Condition header | \n" +
                        " | true |");
        assertThat(match, notNullValue());
        assertThat(match.getDataTable(), hasSize(1));
        assertThat(match.getDataTable().get(0), aMapWithSize(1));
        assertThat(match.getDataTable().get(0), hasEntry("condition",
                ValueMatch.builder().value("true").build()));
    }

    @Test
    public void testSimplePattern() {
        final StepSpec<JsonSchema> stepSpec = StepSpec.<JsonSchema>builder().pattern("{name} has type {type} with value {value}").
                patternType(PatternType.SIMPLE).
                argument(ArgumentSpec.<JsonSchema>builder().name("name").schema(JsonSchema.ofType(STRING)).build()).
                argument(ArgumentSpec.<JsonSchema>builder().name("value").schema(JsonSchema.ofType(BOOLEAN)).build()).
                argument(ArgumentSpec.<JsonSchema>builder().name("type").schema(JsonSchema.ofType(STRING)).build()).
                build();
        // Positive match
        final StepMatch match = this.patternMatcher.matches(stepSpec,
                "\"Peter\" has type \"Human\" with value true");
        assertThat(match, notNullValue());
        assertThat(match.getArguments(), aMapWithSize(3));
        assertThat(match.getArguments(), hasEntry("name",
                ValueMatch.<CompiledExpression>builder().value(
                        patternMatcher.evaluator.match("\"Peter\"").getCompiledExpression()
                ).desiredSchema(JsonSchema.ofType(STRING)).build()));
        assertThat(match.getArguments(), hasEntry("type",
                ValueMatch.builder().value(
                        patternMatcher.evaluator.match("\"Human\"").getCompiledExpression()
                ).desiredSchema(JsonSchema.ofType(STRING)).build()));
        assertThat(match.getArguments(), hasEntry("value",
                ValueMatch.builder().value(
                        patternMatcher.evaluator.match("true").getCompiledExpression()
                ).desiredSchema(JsonSchema.ofType(BOOLEAN)).build()));
        // Negative match
        assertThat(this.patternMatcher.matches(stepSpec, "some other text"), nullValue());
    }

    @Test
    public void testSimplePatternGreadyAttributes() {
        final StepSpec<JsonSchema> stepSpec = StepSpec.<JsonSchema>builder().pattern("I {name} has type {type}").
                patternType(PatternType.SIMPLE).
                argument(ArgumentSpec.<JsonSchema>builder().name("name").schema(JsonSchema.ofType(STRING)).build()).
                argument(ArgumentSpec.<JsonSchema>builder().name("type").schema(JsonSchema.ofType(STRING)).build()).
                build();
        // Positive match
        final StepMatch match = this.patternMatcher.matches(stepSpec,
                "I \"Peter\" and Ema has type \"humans\"");
        assertThat(match, nullValue());
    }
}