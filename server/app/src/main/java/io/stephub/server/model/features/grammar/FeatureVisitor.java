package io.stephub.server.model.features.grammar;

import io.stephub.server.api.model.gherkin.Annotatable;
import io.stephub.server.api.model.gherkin.Feature;
import io.stephub.server.api.model.gherkin.Scenario;
import io.stephub.server.api.model.gherkin.StepSequence;
import io.stephub.server.model.features.generated.FeaturesBaseVisitor;
import io.stephub.server.model.features.generated.FeaturesParser;

import java.util.List;

import static io.stephub.server.service.GherkinPatternMatcher.DOC_STRING_MARKER;
import static io.stephub.server.service.GherkinPatternMatcher.extractSpaceOffset;

public class FeatureVisitor extends FeaturesBaseVisitor<Feature> {
    @Override
    public Feature visitFeature(final FeaturesParser.FeatureContext ctx) {
        final Feature feature = new Feature();
        feature.setName(ctx.featureHeader().name().getText().trim());
        if (!ctx.featureHeader().annotationLine().isEmpty()) {
            this.parseAnnotations(ctx.featureHeader().annotationLine(),
                    feature);
        }
        if (ctx.featureHeader().background() != null) {
            this.parseBackend(feature, ctx.featureHeader().background());
        }
        if (!ctx.scenario().isEmpty()) {
            this.parseScenarios(feature, ctx.scenario());
        }
        return feature;
    }

    private void parseScenarios(final Feature feature, final List<FeaturesParser.ScenarioContext> scenarios) {
        scenarios.forEach(scenarioContext ->
                feature.getScenarios().add(this.parseScenario(scenarioContext))
        );
    }

    private Scenario parseScenario(final FeaturesParser.ScenarioContext scenarioContext) {
        final Scenario scenario = new Scenario();
        scenario.setName(scenarioContext.name().getText().trim());
        if (!scenarioContext.annotationLine().isEmpty()) {
            this.parseAnnotations(scenarioContext.annotationLine(),
                    scenario);
        }
        if (scenarioContext.steps() != null) {
            this.parseSteps(scenario, scenarioContext.steps());
        }
        return scenario;
    }

    private void parseBackend(final Feature feature, final FeaturesParser.BackgroundContext background) {
        final StepSequence stepSequence = new StepSequence();
        if (background.steps() != null) {
            this.parseSteps(stepSequence, background.steps());
        }
        feature.setBackground(stepSequence);
    }

    private void parseSteps(final StepSequence stepSequence, final FeaturesParser.StepsContext steps) {
        steps.step().forEach(stepContext ->
                stepSequence.getSteps().add(this.normalizeStep(stepContext, stepContext.getText().trim()))
        );
    }

    private String normalizeStep(final FeaturesParser.StepContext stepContext, final String step) {
        final String[] lines = step.split("\n");
        final StringBuilder nStep = new StringBuilder();
        final boolean hasDocString = stepContext.docString() != null;
        boolean insideDocString = false;
        int docStringPadding = 0;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (i > 0) {
                nStep.append("\n");
            }
            if (hasDocString) {
                if (DOC_STRING_MARKER.matcher(line).matches()) {
                    if (!insideDocString) {
                        docStringPadding = line.indexOf("\"\"\"");
                        insideDocString = true;
                    }
                }
            }
            if (hasDocString && insideDocString) {
                line = extractSpaceOffset(line, docStringPadding);
            } else {
                line = line.trim();
            }
            nStep.append(line);
        }
        return nStep.toString().trim();
    }

    private void parseAnnotations(final List<FeaturesParser.AnnotationLineContext> annotations,
                                  final Annotatable target) {
        annotations.forEach(a -> {
            if (!a.tag().isEmpty()) {
                target.getTags().clear();
                a.tag().stream().
                        map(tagContext -> "@" + tagContext.tagName().getText().trim()).
                        forEach(t -> target.getTags().add(t));
            } else if (a.commentLine() != null) {
                target.getComments().add(a.commentLine().comment().getText().trim().trim());
            }
        });
    }
}
