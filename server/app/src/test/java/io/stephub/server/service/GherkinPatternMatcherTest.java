package io.stephub.server.service;

import io.stephub.expression.CompiledExpression;
import io.stephub.expression.ParseException;
import io.stephub.json.schema.JsonSchema;
import io.stephub.provider.api.model.spec.*;
import io.stephub.provider.api.model.spec.DataTableSpec.ColumnSpec;
import io.stephub.server.api.model.GherkinPreferences;
import io.stephub.server.service.GherkinPatternMatcher.StepMatch;
import io.stephub.server.service.GherkinPatternMatcher.ValueMatch;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static io.stephub.json.Json.JsonType.*;
import static io.stephub.json.schema.JsonSchema.ofType;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {GherkinPatternMatcher.class, SimplePatternExtractor.class})
@Slf4j
public class GherkinPatternMatcherTest {
    @Autowired
    private GherkinPatternMatcher patternMatcher;

    private final GherkinPreferences defaultPreferences = new GherkinPreferences() {
    };

    @Test
    public void testDocString() {
        final DocStringSpec<JsonSchema> docStringSpec = DocStringSpec.<JsonSchema>builder().schema(ofType(STRING)).build();
        final StepSpec<JsonSchema> stepSpec = StepSpec.<JsonSchema>builder().pattern("Do with DocString payload").
                patternType(PatternType.SIMPLE).
                payload(StepSpec.PayloadType.DOC_STRING).
                docString(docStringSpec).
                build();
        final StepMatch match = this.patternMatcher.matches(this.defaultPreferences, stepSpec,
                "When do with DocString payload\n" +
                        "  \"\"\"\n" +
                        "  My doc string line 1\n" +
                        "  My doc string line 2\n" +
                        "  \"\"\"");
        assertThat(match, notNullValue());
        assertThat(match.getDocString(), equalTo(
                ValueMatch.builder().spec(docStringSpec).value(
                        "My doc string line 1\n" +
                                "My doc string line 2").build()));
    }

    @Test
    public void testDocStringWithoutDetailSpec() {
        final StepSpec<JsonSchema> stepSpec = StepSpec.<JsonSchema>builder().pattern("Do with DocString payload").
                patternType(PatternType.SIMPLE).
                payload(StepSpec.PayloadType.DOC_STRING).
                build();
        final StepMatch match = this.patternMatcher.matches(this.defaultPreferences, stepSpec,
                "When do with DocString payload\n" +
                        "  \"\"\"\n" +
                        "  My doc string line 1\n" +
                        "  My doc string line 2\n" +
                        "  \"\"\"");
        assertThat(match, notNullValue());
        assertThat(match.getDocString(), equalTo(
                ValueMatch.builder().
                        spec(DocStringSpec.<JsonSchema>builder().schema(JsonSchema.ofType(ANY)).build()).
                        value(
                                "My doc string line 1\n" +
                                        "My doc string line 2").build()));
    }

    @Test
    public void testDocStringMissingOffset() {
        final DocStringSpec<JsonSchema> docStringSpec =
                DocStringSpec.<JsonSchema>builder().schema(ofType(STRING)).build();
        final StepSpec<JsonSchema> stepSpec = StepSpec.<JsonSchema>builder().pattern("Do with DocString payload").
                patternType(PatternType.SIMPLE).
                payload(StepSpec.PayloadType.DOC_STRING).
                docString(docStringSpec).
                build();
        final StepMatch match = this.patternMatcher.matches(this.defaultPreferences, stepSpec,
                "When do with DocString payload\n" +
                        "  \"\"\"\n" +
                        "My doc string line 1\n" +
                        "My doc string line 2\n" +
                        "\"\"\"");
        assertThat(match, notNullValue());
        assertThat(match.getDocString(), equalTo(
                ValueMatch.builder().spec(docStringSpec).value(
                        "My doc string line 1\n" +
                                "My doc string line 2").build()));
    }

    @Test
    public void testDocMissingEndDelimiter() {
        final StepSpec<JsonSchema> stepSpec = StepSpec.<JsonSchema>builder().pattern("Do with DocString payload").
                patternType(PatternType.SIMPLE).
                payload(StepSpec.PayloadType.DOC_STRING).
                build();
        final ParseException e = assertThrows(ParseException.class, () -> {
            this.patternMatcher.matches(this.defaultPreferences, stepSpec,
                    "When do with DocString payload\n" +
                            "  \"\"\"\n" +
                            "My doc string line 1\n");
        });
        log.debug("Exception output", e);
    }

    @Test
    public void testMissingDocString() {
        final StepSpec<JsonSchema> stepSpec = StepSpec.<JsonSchema>builder().pattern("Do with DocString payload").
                patternType(PatternType.SIMPLE).
                payload(StepSpec.PayloadType.DOC_STRING).
                build();
        final ParseException e = assertThrows(ParseException.class, () -> {
            this.patternMatcher.matches(this.defaultPreferences, stepSpec,
                    "When do with DocString payload");
        });
        log.debug("Exception output", e);
    }

    @Test
    public void testMissingDocStringStartDelimiter() {
        final StepSpec<JsonSchema> stepSpec = StepSpec.<JsonSchema>builder().pattern("Do with DocString payload").
                patternType(PatternType.SIMPLE).
                payload(StepSpec.PayloadType.DOC_STRING).
                build();
        final ParseException e = assertThrows(ParseException.class, () -> {
            this.patternMatcher.matches(this.defaultPreferences, stepSpec,
                    "Given do with DocString payload\n-Invalid delimiter");
        });
        log.debug("Exception output", e);
    }

    @Test
    public void testDataTableSingleCol() {
        final ColumnSpec<JsonSchema> colSpec = ColumnSpec.<JsonSchema>builder().
                name("condition").
                build();
        final StepSpec<JsonSchema> stepSpec = StepSpec.<JsonSchema>builder().pattern("Do with DocString payload").
                patternType(PatternType.SIMPLE).
                payload(StepSpec.PayloadType.DATA_TABLE).
                dataTable(
                        DataTableSpec.<JsonSchema>builder().
                                header(false).
                                column(
                                        colSpec
                                ).
                                build()
                ).build();
        final StepMatch match = this.patternMatcher.matches(this.defaultPreferences, stepSpec,
                "When do with DocString payload\n" +
                        "| true |");
        assertThat(match, notNullValue());
        assertThat(match.getDataTable(), hasSize(1));
        assertThat(match.getDataTable().get(0), aMapWithSize(1));
        assertThat(match.getDataTable().get(0), hasEntry("condition",
                ValueMatch.builder().value("true").spec(colSpec).build()));
    }


    @Test
    public void testDataTableSingleColWithCommentAndEmptyLines() {
        final ColumnSpec<JsonSchema> colSpec = ColumnSpec.<JsonSchema>builder().
                name("condition").
                build();
        final StepSpec<JsonSchema> stepSpec = StepSpec.<JsonSchema>builder().pattern("Do with DocString payload").
                patternType(PatternType.SIMPLE).
                payload(StepSpec.PayloadType.DATA_TABLE).
                dataTable(
                        DataTableSpec.<JsonSchema>builder().
                                header(false).
                                column(
                                        colSpec
                                ).
                                build()
                ).build();
        final StepMatch match = this.patternMatcher.matches(this.defaultPreferences, stepSpec,
                "  \n " +
                        "# Step with data table \n \n " +
                        " Then Do with DocString payload\n" +
                        "   # Some comment\n" +
                        "| true |\n" +
                        " | false | \n\n");
        assertThat(match, notNullValue());
        assertThat(match.getDataTable(), hasSize(2));
        assertThat(match.getDataTable().get(0), aMapWithSize(1));
        assertThat(match.getDataTable().get(0), hasEntry("condition",
                ValueMatch.builder().value("true").spec(colSpec).build()));
        assertThat(match.getDataTable().get(1), aMapWithSize(1));
        assertThat(match.getDataTable().get(1), hasEntry("condition",
                ValueMatch.builder().value("false").spec(colSpec).build()));
    }

    @Test
    public void testDataTableMultipleCols() {
        final ColumnSpec<JsonSchema> col1Spec = ColumnSpec.<JsonSchema>builder().
                name("condition").
                build();
        final ColumnSpec<JsonSchema> col2Spec = ColumnSpec.<JsonSchema>builder().
                name("text").
                build();
        final StepSpec<JsonSchema> stepSpec = StepSpec.<JsonSchema>builder().pattern("Do with DocString payload").
                patternType(PatternType.SIMPLE).
                payload(StepSpec.PayloadType.DATA_TABLE).
                dataTable(
                        DataTableSpec.<JsonSchema>builder().
                                header(false).
                                column(
                                        col1Spec
                                ).
                                column(
                                        col2Spec
                                ).
                                build()
                ).build();
        final StepMatch match = this.patternMatcher.matches(this.defaultPreferences, stepSpec,
                "When do with DocString payload\n" +
                        "   | true  | my text   | \n" +
                        "| false | next text | \n");
        assertThat(match, notNullValue());
        assertThat(match.getDataTable(), hasSize(2));
        assertThat(match.getDataTable().get(0), aMapWithSize(2));
        assertThat(match.getDataTable().get(0), hasEntry("condition",
                ValueMatch.builder().value("true").spec(col1Spec).build()));
        assertThat(match.getDataTable().get(0), hasEntry("text",
                ValueMatch.builder().value("my text").spec(col2Spec).build()));
        assertThat(match.getDataTable().get(1), aMapWithSize(2));
        assertThat(match.getDataTable().get(1), hasEntry("condition",
                ValueMatch.builder().value("false").spec(col1Spec).build()));
        assertThat(match.getDataTable().get(1), hasEntry("text",
                ValueMatch.builder().value("next text").spec(col2Spec).build()));
    }

    @Test
    public void testDataTableInvalidCols() {
        final StepSpec<JsonSchema> stepSpec = StepSpec.<JsonSchema>builder().pattern("Do with DocString payload").
                patternType(PatternType.SIMPLE).
                payload(StepSpec.PayloadType.DATA_TABLE).
                dataTable(
                        DataTableSpec.<JsonSchema>builder().
                                header(false).
                                column(
                                        ColumnSpec.<JsonSchema>builder().
                                                name("condition").
                                                build()
                                ).
                                column(
                                        ColumnSpec.<JsonSchema>builder().
                                                name("text").
                                                build()
                                ).
                                build()
                ).build();
        final ParseException e = assertThrows(ParseException.class, () -> {
            final StepMatch match = this.patternMatcher.matches(this.defaultPreferences, stepSpec,
                    "When do with DocString payload\n" +
                            "   | true  | my text   | \n" +
                            "| false | \n" +
                            " | false | abc |");
        });
        log.debug("Exception output", e);
    }


    @Test
    public void testDataTableSingleColWithHeader() {
        final StepSpec<JsonSchema> stepSpec = StepSpec.<JsonSchema>builder().pattern("Do with DocString payload").
                patternType(PatternType.SIMPLE).
                payload(StepSpec.PayloadType.DATA_TABLE).
                dataTable(
                        DataTableSpec.<JsonSchema>builder().
                                header(true).
                                column(
                                        ColumnSpec.<JsonSchema>builder().
                                                name("condition").
                                                build()
                                ).
                                build()
                ).build();
        final StepMatch match = this.patternMatcher.matches(this.defaultPreferences, stepSpec,
                "When do with DocString payload\n" +
                        " | Condition header | \n" +
                        " | true |");
        assertThat(match, notNullValue());
        assertThat(match.getDataTable(), hasSize(1));
        assertThat(match.getDataTable().get(0), aMapWithSize(1));
        assertThat(match.getDataTable().get(0), hasEntry("condition",
                ValueMatch.builder().
                        spec(stepSpec.getDataTable().getColumns().get(0)).
                        value("true").build()));
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
        final StepMatch match = this.patternMatcher.matches(this.defaultPreferences, stepSpec,
                "Given \"Peter\" has type \"Human\" with value true");
        assertThat(match, notNullValue());
        assertThat(match.getArguments(), aMapWithSize(3));
        assertThat(match.getArguments(), hasEntry("name",
                ValueMatch.<CompiledExpression>builder().value(
                        this.patternMatcher.evaluator.match("\"Peter\"").getCompiledExpression()
                ).spec(stepSpec.getArguments().get(0)).build()));
        assertThat(match.getArguments(), hasEntry("type",
                ValueMatch.builder().value(
                        this.patternMatcher.evaluator.match("\"Human\"").getCompiledExpression()
                ).spec(stepSpec.getArguments().get(2)).build()));
        assertThat(match.getArguments(), hasEntry("value",
                ValueMatch.builder().value(
                        this.patternMatcher.evaluator.match("true").getCompiledExpression()
                ).spec(stepSpec.getArguments().get(1)).build()));
        // Negative match
        assertThat(this.patternMatcher.matches(this.defaultPreferences, stepSpec, "some other text"), nullValue());
    }

    @Test
    public void testSimplePatternGreadyAttributes() {
        final StepSpec<JsonSchema> stepSpec = StepSpec.<JsonSchema>builder().pattern("I {name} has type {type}").
                patternType(PatternType.SIMPLE).
                argument(ArgumentSpec.<JsonSchema>builder().name("name").schema(JsonSchema.ofType(STRING)).build()).
                argument(ArgumentSpec.<JsonSchema>builder().name("type").schema(JsonSchema.ofType(STRING)).build()).
                build();
        // Positive match
        final StepMatch match = this.patternMatcher.matches(this.defaultPreferences, stepSpec,
                "When I \"Peter\" and Ema has type \"humans\"");
        assertThat(match, nullValue());
    }

    @Test
    public void testSimplePatternWithOutput() {
        final StepSpec<JsonSchema> stepSpec = StepSpec.<JsonSchema>builder().pattern("value {__value}").
                patternType(PatternType.SIMPLE).
                argument(ArgumentSpec.<JsonSchema>builder().name("__value").schema(JsonSchema.ofType(NUMBER)).build()).
                output(OutputSpec.<JsonSchema>builder().schema(JsonSchema.ofType(ANY)).build()).
                build();
        // Match with output assignment
        final StepMatch match1 = this.patternMatcher.matches(this.defaultPreferences, stepSpec,
                "Given value 3 - assigned to ${abc}");
        assertThat(match1, notNullValue());
        assertThat(match1.getArguments(), hasKey("__value"));
        assertThat(match1.getOutputAssignment(), notNullValue());
        assertThat(match1.getOutputAssignment().getValue().isAssignable(), equalTo(true));

        // Match without output assignment
        final StepMatch match2 = this.patternMatcher.matches(this.defaultPreferences, stepSpec,
                "Given value 3");
        assertThat(match2, notNullValue());
        assertThat(match2.getOutputAssignment(), nullValue());
    }
}