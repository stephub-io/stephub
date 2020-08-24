package io.stephub.server.service;

import io.stephub.expression.CompiledExpression;
import io.stephub.expression.ExpressionEvaluator;
import io.stephub.expression.MatchResult;
import io.stephub.expression.ParseException;
import io.stephub.expression.impl.DefaultExpressionEvaluator;
import io.stephub.json.Json;
import io.stephub.json.schema.JsonSchema;
import io.stephub.provider.api.model.spec.*;
import io.stephub.server.api.model.GherkinPreferences;
import io.stephub.server.api.model.Workspace;
import io.stephub.server.api.model.gherkin.Feature;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.stephub.provider.api.model.spec.StepSpec.PayloadType.DATA_TABLE;
import static io.stephub.provider.api.model.spec.StepSpec.PayloadType.DOC_STRING;
import static io.stephub.server.service.SimplePatternExtractor.DEFAULT_PATTERN_FLAGS;


@Service
public class GherkinPatternMatcher {
    public static final Pattern DOC_STRING_MARKER = Pattern.compile("(\\s*)\"\"\"\\s*");
    private static final Pattern SKIP_LINES_PATTERN = Pattern.compile("^(\\s*#.*|\\s*)$");

    final ExpressionEvaluator evaluator = new DefaultExpressionEvaluator();

    @Autowired
    private SimplePatternExtractor simplePatternExtractor;

    @Getter
    @Builder
    @EqualsAndHashCode
    @ToString
    public static class StepMatch {
        @Singular
        private final Map<String, ValueMatch<CompiledExpression>> arguments;
        private final ValueMatch<String> docString;
        private final List<Map<String, ValueMatch<String>>> dataTable;
        private final String outputAssignmentAttribute;
    }


    @Getter
    @Builder
    @EqualsAndHashCode
    @ToString
    public static class ValueMatch<V> {
        private final V value;
        private final ValueSpec<JsonSchema> spec;
    }

    public StepMatch matches(final GherkinPreferences preferences, final StepSpec<JsonSchema> stepSpec, final String instruction) {
        Pattern pattern = null;
        List<ArgumentSpec<JsonSchema>> additionalArguments = null;
        switch (stepSpec.getPatternType()) {
            case SIMPLE:
                final SimplePatternExtractor.Extraction simplePatternExtraction = this.simplePatternExtractor.extract(preferences, stepSpec.getPattern(), stepSpec.getOutput() != null);
                additionalArguments = simplePatternExtraction.getArguments();
                pattern = simplePatternExtraction.getRegexPattern();
            case REGEX:
                final String[] linesRaw = instruction.split("\r?\n");
                final List<String> effectiveLines = new ArrayList<>();
                for (final String line : linesRaw) {
                    if (!SKIP_LINES_PATTERN.matcher(line).matches()) {
                        effectiveLines.add(line);
                    }
                }
                final String[] lines = effectiveLines.toArray(new String[effectiveLines.size()]);
                if (lines.length == 0) {
                    throw new ParseException("Passed instruction doesn't contain any step: " + instruction);
                }
                if (pattern == null) {
                    pattern = Pattern.compile(preferences.surround(stepSpec.getPattern(), stepSpec.getOutput() != null), DEFAULT_PATTERN_FLAGS);
                }
                final Matcher matcher = pattern.matcher(lines[0].trim());
                if (matcher.matches()) {
                    final StepMatch.StepMatchBuilder stepMatchBuilder = StepMatch.builder();
                    final Map<String, ArgumentSpec<JsonSchema>> arguments = new HashMap<>();
                    if (additionalArguments != null) {
                        additionalArguments.forEach(a -> arguments.put(a.getName(), a));
                    }
                    if (stepSpec.getArguments() != null) {
                        stepSpec.getArguments().forEach(a -> arguments.put(a.getName(), a));
                    }
                    for (final ArgumentSpec<JsonSchema> a : arguments.values()) {
                        final String argValue = matcher.group(a.getName());
                        final MatchResult argExprMatcher = this.evaluator.match(argValue);
                        if (!argExprMatcher.matches()) {
                            return null;
                        }
                        stepMatchBuilder.argument(
                                a.getName(),
                                ValueMatch.<CompiledExpression>builder().
                                        value(argExprMatcher.getCompiledExpression()).
                                        spec(a).
                                        build());
                    }

                    this.checkAndExtractPayload(stepSpec, instruction, lines, stepMatchBuilder);
                    if (stepSpec.getOutput() != null) {
                        final String outputAttribute = matcher.group(GherkinPreferences.OUTPUT_ATTRIBUTE_GROUP_NAME);
                        if (StringUtils.isNotBlank(outputAttribute)) {
                            stepMatchBuilder.outputAssignmentAttribute(outputAttribute);
                        }
                    }
                    return stepMatchBuilder.build();
                } else {
                    return null;
                }
        }
        throw new UnsupportedOperationException("Pattern matching not implemented for type=" + stepSpec.getPatternType());
    }

    private void checkAndExtractPayload(final StepSpec<JsonSchema> stepSpec, final String instruction, final String[] lines, final StepMatch.StepMatchBuilder stepMatchBuilder) {
        if (stepSpec.getPayload() == DOC_STRING) {
            this.checkAndExtractDocString(stepSpec, instruction, lines, stepMatchBuilder);
        } else if (stepSpec.getPayload() == DATA_TABLE) {
            this.checkAndExtractDataTable(stepSpec, instruction, lines, stepMatchBuilder);
        }
    }

    private void checkAndExtractDataTable(final StepSpec<JsonSchema> stepSpec, final String instruction, final String[] lines, final StepMatch.StepMatchBuilder stepMatchBuilder) {
        final List<Map<String, ValueMatch<String>>> rows = new ArrayList<>();
        final int cols = stepSpec.getDataTable().getColumns().size();
        final StringBuilder rowPatternStr = new StringBuilder();
        rowPatternStr.append("\\s*\\|\\s*");
        for (int i = 0; i < cols; i++) {
            rowPatternStr.append("((?:\\\\||[^|])*)\\s*\\|\\s*");
        }
        final Pattern rowPattern = Pattern.compile(rowPatternStr.toString());
        boolean ignoreHeader = stepSpec.getDataTable().isHeader();
        for (int i = 1; i < lines.length; i++) {
            if (lines[i].trim().length() == 0) {
                continue;
            }
            final Matcher matcher = rowPattern.matcher(lines[i]);
            if (matcher.matches()) {
                if (ignoreHeader) {
                    ignoreHeader = false;
                    continue;
                }
                final Map<String, ValueMatch<String>> cells = new HashMap<>();
                for (int j = 0; j < cols; j++) {
                    final DataTableSpec.ColumnSpec<JsonSchema> colSpec = stepSpec.getDataTable().getColumns().get(j);
                    cells.put(colSpec.getName(),
                            ValueMatch.<String>builder().
                                    value(matcher.group(1 + j).trim()).
                                    spec(colSpec).build()
                    );
                }
                rows.add(cells);
            } else {
                throw new ParseException("Row " + i + " in data table should have " + cols + " cells split by '|': " + instruction);
            }
        }
        stepMatchBuilder.dataTable(rows);
    }

    private void checkAndExtractDocString(final StepSpec<JsonSchema> stepSpec, final String instruction, final String[] lines, final StepMatch.StepMatchBuilder stepMatchBuilder) {
        // Check syntax
        if (lines.length < 2) {
            throw new ParseException("DocString expected, but missed in: " + instruction);
        }
        final Matcher markerStart = DOC_STRING_MARKER.matcher(lines[1]);
        if (!markerStart.matches()) {
            throw new ParseException("First line of DocString payload should be offset by delimiters consisting of three double-quote marks (\"\"\"): " + instruction);
        }
        int endLine = 0;
        for (int i = 2; i < lines.length; i++) {
            if (DOC_STRING_MARKER.matcher(lines[i]).matches()) {
                endLine = i;
                break;
            }
        }
        if (endLine == 0) {
            throw new ParseException("Last line of DocString payload not found which should end by delimiters consisting of three double-quote marks (\"\"\"): " + instruction);
        }
        // Syntax ok
        final StringBuilder extraction = new StringBuilder();
        final int offsetCount = markerStart.group(1).length();
        for (int i = 2; i < endLine; i++) {
            extraction.append(this.extractSpaceOffset(lines[i], offsetCount));
            if (i < lines.length - 2) {
                extraction.append("\n");
            }
        }
        stepMatchBuilder.docString(
                ValueMatch.<String>builder().value(extraction.toString()).
                        spec(
                                stepSpec.getDocString() != null ? stepSpec.getDocString() :
                                        DocStringSpec.<JsonSchema>builder().schema(JsonSchema.ofType(Json.JsonType.ANY)).build()
                        ).
                        build());
    }

    public static String extractSpaceOffset(final String str, int maxOffset) {
        maxOffset = Math.min(str.length(), maxOffset);
        for (int i = 0; i < maxOffset; i++) {
            if (str.charAt(i) != ' ') {
                return str.substring(i);
            }
        }
        return str.substring(maxOffset);
    }
}
