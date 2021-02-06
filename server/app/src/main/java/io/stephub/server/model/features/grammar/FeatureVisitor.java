package io.stephub.server.model.features.grammar;

import io.stephub.server.api.model.gherkin.Feature;
import io.stephub.server.api.model.gherkin.Scenario;
import io.stephub.server.api.model.gherkin.StepSequence;
import io.stephub.server.model.features.generated.FeaturesBaseVisitor;
import io.stephub.server.model.features.generated.FeaturesParser;

import java.util.List;
import java.util.function.Consumer;

import static io.stephub.server.service.GherkinPatternMatcher.DOC_STRING_MARKER;
import static io.stephub.server.service.GherkinPatternMatcher.extractSpaceOffset;

public class FeatureVisitor extends FeaturesBaseVisitor<Feature> {
    @Override
    public Feature visitFeature(final FeaturesParser.FeatureContext ctx) {
        final Feature.FeatureBuilder builder = Feature.builder();
        builder.name(ctx.featureHeader().name().getText().trim());
        if (!ctx.featureHeader().annotationLine().isEmpty()) {
            this.parseAnnotations(ctx.featureHeader().annotationLine(),
                    builder::tag, builder::comment);
        }
        if (ctx.featureHeader().background() != null) {
            this.parseBackend(builder, ctx.featureHeader().background());
        }
        if (!ctx.scenario().isEmpty()) {
            this.parseScenarios(builder, ctx.scenario());
        }
        return builder.build();
    }

    private void parseScenarios(final Feature.FeatureBuilder builder, final List<FeaturesParser.ScenarioContext> scenarios) {
        scenarios.forEach(scenarioContext ->
                builder.scenario(this.parseScenario(scenarioContext))
        );
    }

    private Scenario parseScenario(final FeaturesParser.ScenarioContext scenarioContext) {
        final Scenario.ScenarioBuilder<?, ?> builder = Scenario.builder().name(scenarioContext.name().getText().trim());
        if (!scenarioContext.annotationLine().isEmpty()) {
            this.parseAnnotations(scenarioContext.annotationLine(),
                    builder::tag, builder::comment);
        }
        if (scenarioContext.steps() != null) {
            this.parseSteps(builder, scenarioContext.steps());
        }
        return builder.build();
    }

    private void parseBackend(final Feature.FeatureBuilder builder, final FeaturesParser.BackgroundContext background) {
        final StepSequence.StepSequenceBuilder<?, ?> stepSequenceBuilder = StepSequence.builder();
        if (background.steps() != null) {
            this.parseSteps(stepSequenceBuilder, background.steps());
        }
        builder.background(stepSequenceBuilder.build());
    }

    private void parseSteps(final StepSequence.StepSequenceBuilder<?, ?> stepSequenceBuilder, final FeaturesParser.StepsContext steps) {
        steps.step().forEach(stepContext ->
                stepSequenceBuilder.step(this.normalizeStep(stepContext, stepContext.getText().trim()))
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
                                  final Consumer<String> tagConsumer, final Consumer<String> commentConsumer) {
        annotations.forEach(a -> {
            if (!a.tag().isEmpty()) {
                a.tag().stream().
                        map(tagContext -> "@" + tagContext.tagName().getText().trim()).
                        forEach(tagConsumer);
            } else if (a.commentLine() != null) {
                commentConsumer.accept(a.commentLine().comment().getText().trim().trim());
            }
        });
    }
}
