package io.stephub.runtime.service;

import io.stephub.expression.ParseException;
import io.stephub.json.schema.JsonSchema;
import io.stephub.provider.spec.*;
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
        final StepSpec stepSpec = StepSpec.builder().pattern("Do with DocString payload").
                patternType(PatternType.REGEX).
                payload(StepSpec.PayloadType.DOC_STRING).
                docString(DocStringSpec.builder().schema(ofType(STRING)).build()).
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
        final StepSpec stepSpec = StepSpec.builder().pattern("Do with DocString payload").
                patternType(PatternType.REGEX).
                payload(StepSpec.PayloadType.DOC_STRING).
                docString(DocStringSpec.builder().schema(ofType(STRING)).build()).
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
        final StepSpec stepSpec = StepSpec.builder().pattern("Do with DocString payload").
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
        final StepSpec stepSpec = StepSpec.builder().pattern("Do with DocString payload").
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
        final StepSpec stepSpec = StepSpec.builder().pattern("Do with DocString payload").
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
        final StepSpec stepSpec = StepSpec.builder().pattern("Do with DocString payload").
                patternType(PatternType.REGEX).
                payload(StepSpec.PayloadType.DATA_TABLE).
                dataTable(
                        DataTableSpec.builder().
                                header(false).
                                column(
                                        DataTableSpec.ColumnSpec.builder().
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
        final StepSpec stepSpec = StepSpec.builder().pattern("Do with DocString payload").
                patternType(PatternType.REGEX).
                payload(StepSpec.PayloadType.DATA_TABLE).
                dataTable(
                        DataTableSpec.builder().
                                header(false).
                                column(
                                        DataTableSpec.ColumnSpec.builder().
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
        final StepSpec stepSpec = StepSpec.builder().pattern("Do with DocString payload").
                patternType(PatternType.REGEX).
                payload(StepSpec.PayloadType.DATA_TABLE).
                dataTable(
                        DataTableSpec.builder().
                                header(false).
                                column(
                                        DataTableSpec.ColumnSpec.builder().
                                                name("condition").
                                                build()
                                ).
                                column(
                                        DataTableSpec.ColumnSpec.builder().
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
        final StepSpec stepSpec = StepSpec.builder().pattern("Do with DocString payload").
                patternType(PatternType.REGEX).
                payload(StepSpec.PayloadType.DATA_TABLE).
                dataTable(
                        DataTableSpec.builder().
                                header(false).
                                column(
                                        DataTableSpec.ColumnSpec.builder().
                                                name("condition").
                                                build()
                                ).
                                column(
                                        DataTableSpec.ColumnSpec.builder().
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
        final StepSpec stepSpec = StepSpec.builder().pattern("Do with DocString payload").
                patternType(PatternType.REGEX).
                payload(StepSpec.PayloadType.DATA_TABLE).
                dataTable(
                        DataTableSpec.builder().
                                header(true).
                                column(
                                        DataTableSpec.ColumnSpec.builder().
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
        final StepSpec stepSpec = StepSpec.builder().pattern("{name} has type {type} with value {value}").
                patternType(PatternType.SIMPLE).
                argument(ArgumentSpec.builder().name("name").schema(JsonSchema.ofType(STRING)).build()).
                argument(ArgumentSpec.builder().name("value").schema(JsonSchema.ofType(BOOLEAN)).build()).
                argument(ArgumentSpec.builder().name("type").schema(JsonSchema.ofType(STRING)).build()).
                build();
        // Positive match
        final StepMatch match = this.patternMatcher.matches(stepSpec,
                "\"Peter\" has type \"Human\" with value true");
        assertThat(match, notNullValue());
        assertThat(match.getArguments(), aMapWithSize(3));
        assertThat(match.getArguments(), hasEntry("name",
                ValueMatch.builder().value("\"Peter\"").desiredSchema(JsonSchema.ofType(STRING)).build()));
        assertThat(match.getArguments(), hasEntry("type",
                ValueMatch.builder().value("\"Human\"").desiredSchema(JsonSchema.ofType(STRING)).build()));
        assertThat(match.getArguments(), hasEntry("value",
                ValueMatch.builder().value("true").desiredSchema(JsonSchema.ofType(BOOLEAN)).build()));
        // Negative match
        assertThat(this.patternMatcher.matches(stepSpec, "some other text"), nullValue());
    }
}