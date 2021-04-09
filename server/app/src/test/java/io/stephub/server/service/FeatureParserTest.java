package io.stephub.server.service;

import io.stephub.server.api.model.gherkin.Feature;
import io.stephub.server.api.model.gherkin.Scenario;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@Slf4j
public class FeatureParserTest {

    private final FeatureParser parser = new FeatureParser();

    @Test
    public void testSimpleFeature() {
        final Feature feature = this.parser.parseFeature(null, "Feature: Abc");
        assertThat(feature.getName(), equalTo("Abc"));
        assertThat(feature.getBackground(), nullValue());
        assertThat(feature.getTags(), hasSize(1));
        assertThat(feature.getTags(), hasItem(Feature.DEFAULT_TAG));
        assertThat(feature.getScenarios(), hasSize(0));
    }

    @Test
    public void testSimpleFeatureTaggedAndCommented() {
        final Feature feature = this.parser.parseFeature(null, "# first comment\n" +
                "@tag1 \t@tag-xy\n\n" +
                "\t@tag87#comment\n" +
                "# second comment # doubled \n" +
                "\tFeature: Abc");
        assertThat(feature.getName(), equalTo("Abc"));
        assertThat(feature.getBackground(), nullValue());
        assertThat(feature.getTags(), hasSize(3));
        assertThat(feature.getTags(), hasItem("@tag1"));
        assertThat(feature.getTags(), hasItem("@tag-xy"));
        assertThat(feature.getTags(), hasItem("@tag87"));
        assertThat(feature.getComments(), hasSize(2));
        assertThat(feature.getComments(), hasItem("first comment"));
        assertThat(feature.getComments(), hasItem("second comment # doubled"));
        assertThat(feature.getScenarios(), hasSize(0));
    }

    @Test
    public void testBackground() {
        final Feature feature = this.parser.parseFeature(null, "# first comment\n" +
                "@tag1 \t@tag-xy\n\n" +
                "\t@tag87#comment\n" +
                "# second comment \n" +
                "\tFeature: Abc\n" +
                "\t Background:\n\n" +
                " # Just a comment \n\n" +
                " Given I test something");
        assertThat(feature.getName(), equalTo("Abc"));
        assertThat(feature.getBackground(), notNullValue());
        assertThat(feature.getBackground().getSteps(), hasSize(1));
        assertThat(feature.getBackground().getSteps(), hasItem("# Just a comment\n\nGiven I test something"));
        assertThat(feature.getTags(), hasSize(3));
        assertThat(feature.getComments(), hasSize(2));
        assertThat(feature.getScenarios(), hasSize(0));
    }

    @Test
    public void testMultipleSteps() {
        final Feature feature = this.parser.parseFeature(null,
                "Feature: Abc\n" +
                        "Background:\n" +
                        " # Just a comment \n\n" +
                        " Given I test something\n" +
                        "# Comment of next step # NOPE\n" +
                        "# One comment more \n" +
                        "The I have next step");
        assertThat(feature.getBackground().getSteps(), hasSize(2));
        assertThat(feature.getBackground().getSteps(), hasItem("# Just a comment\n\nGiven I test something"));
        assertThat(feature.getBackground().getSteps(), hasItem("# Comment of next step # NOPE\n# One comment more\nThe I have next step"));
    }

    @Test
    public void testStepsTerminatingComment() {
        final Feature feature = this.parser.parseFeature(null,
                "Feature: Abc\n" +
                        "Background:\n" +
                        " # Just a start comment \n\n" +
                        " Given I test something\n" +
                        "# Final comment\n");
        assertThat(feature.getBackground().getSteps(), hasSize(1));
        assertThat(feature.getBackground().getSteps(), hasItem("# Just a start comment\n\nGiven I test something"));
    }

    @Test
    public void testStepsDocString() {
        final Feature feature = this.parser.parseFeature(null,
                "Feature: Abc\n" +
                        "Background:\n" +
                        " # Just a comment \n\n" +
                        " Given I test something\n" +
                        "# DocString in next line\n\n" +
                        "  \"\"\"\n" +
                        "   My Doc  \n" +
                        " \"\"\"");
        assertThat(feature.getBackground().getSteps(), hasSize(1));
        assertThat(feature.getBackground().getSteps(), hasItem("# Just a comment\n\nGiven I test something\n" +
                "# DocString in next line\n\n" +
                "\"\"\"\n" +
                " My Doc  \n" +
                "\"\"\""));
    }

    @Test
    public void testStepsDataTable() {
        final Feature feature = this.parser.parseFeature(null,
                "Feature: Abc\n" +
                        "Background:\n" +
                        " # Just a comment \n\n" +
                        " Given I test something\n" +
                        "# Data table in next line\n\n" +
                        " | col1 | col2 |  \n" +
                        " # Inner table comment\n" +
                        " | My   | row  |\n\n" +
                        "Next step");
        assertThat(feature.getBackground().getSteps(), hasSize(2));
        assertThat(feature.getBackground().getSteps(), hasItem("# Just a comment\n\nGiven I test something\n" +
                "# Data table in next line\n\n" +
                "| col1 | col2 |\n" +
                "# Inner table comment\n" +
                "| My   | row  |"));
        assertThat(feature.getBackground().getSteps(), hasItem("Next step"));
    }

    @Test
    public void testBackendAndScenarios() {
        final Feature feature = this.parser.parseFeature(null,
                "Feature: Abc\n" +
                        "Background:\n" +
                        " # Just a comment \n\n" +
                        " Given I test something\n\n" +
                        "# Comment for scenario\n" +
                        "@stag1  @stag2\n" +
                        " Scenario:   Def 1\n" +
                        "# Step comment\n" +
                        "Scenario step # hello\n" +
                        "# Comment with direct EOF");
        assertThat(feature.getBackground().getSteps(), hasSize(1));
        assertThat(feature.getBackground().getSteps(), hasItem("# Just a comment\n\nGiven I test something"));
        assertThat(feature.getScenarios(), hasSize(1));
        final Scenario scenario = feature.getScenarios().get(0);
        assertThat(scenario.getName(), equalTo("Def 1"));
        assertThat(scenario.getComments(), hasSize(1));
        assertThat(scenario.getComments(), hasItem("Comment for scenario"));
        assertThat(scenario.getTags(), hasSize(2));
        assertThat(scenario.getTags(), hasItem("@stag1"));
        assertThat(scenario.getTags(), hasItem("@stag2"));
        assertThat(scenario.getSteps(), hasSize(1));
        assertThat(scenario.getSteps(), hasItem("# Step comment\nScenario step # hello"));
    }
}