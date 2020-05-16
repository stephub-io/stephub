package io.stephub.runtime.service;

import io.stephub.expression.CompiledExpression;
import io.stephub.expression.ExpressionEvaluator;
import io.stephub.expression.MatchResult;
import io.stephub.expression.ParseException;
import io.stephub.expression.impl.DefaultExpressionEvaluator;
import io.stephub.json.schema.JsonSchema;
import io.stephub.provider.api.model.spec.ArgumentSpec;
import io.stephub.provider.api.model.spec.DataTableSpec;
import io.stephub.provider.api.model.spec.StepSpec;
import io.stephub.provider.api.model.spec.ValueSpec;
import lombok.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.stephub.provider.api.model.spec.StepSpec.PayloadType.DATA_TABLE;
import static io.stephub.provider.api.model.spec.StepSpec.PayloadType.DOC_STRING;


@Service
public class GherkinPatternMatcher {
    private static final Pattern DOC_STRING_MARKER = Pattern.compile("(\\s*)\"\"\"\\s*");
    private static final Pattern SKIP_LINES_PATTERN = Pattern.compile("^(\\s*#.*|\\s*)$");
    private static final Pattern SIMPLE_PATTERN_ARG_PATTERN = Pattern.compile("(^|[^\\\\])\\{([a-zA-Z_][a-zA-Z0-9_]*)\\}");
    final ExpressionEvaluator evaluator = new DefaultExpressionEvaluator();

    @Getter
    @Builder
    @EqualsAndHashCode
    @ToString
    public static class StepMatch {
        @Singular
        private final Map<String, ValueMatch<CompiledExpression>> arguments;
        private final ValueMatch<String> docString;
        private final List<Map<String, ValueMatch<String>>> dataTable;
    }


    @Getter
    @Builder
    @EqualsAndHashCode
    @ToString
    public static class ValueMatch<V> {
        private final V value;
        private final ValueSpec<JsonSchema> spec;
    }

    public StepMatch matches(final StepSpec<JsonSchema> stepSpec, final String instruction) {
        String patternStr = stepSpec.getPattern();
        switch (stepSpec.getPatternType()) {
            case SIMPLE:
                patternStr = this.convertSimplePatternToRegex(patternStr);
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
                final Pattern pattern = Pattern.compile(patternStr);
                final Matcher matcher = pattern.matcher(lines[0].trim());
                if (matcher.matches()) {
                    final StepMatch.StepMatchBuilder stepMatchBuilder = StepMatch.builder();
                    for (final ArgumentSpec<JsonSchema> a : stepSpec.getArguments()) {
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
                    return stepMatchBuilder.build();
                } else {
                    return null;
                }
        }
        throw new UnsupportedOperationException("Pattern matching not implemented for type=" + stepSpec.getPatternType());
    }

    private String convertSimplePatternToRegex(final String patternStr) {
        final Matcher matcher = SIMPLE_PATTERN_ARG_PATTERN.matcher(patternStr);
        int left = 0;
        final StringBuilder regexPattern = new StringBuilder();
        while (matcher.find()) {
            if (left < matcher.start()) {
                regexPattern.append(Pattern.quote(patternStr.substring(left, matcher.start()) + matcher.group(1)));
            }
            regexPattern.append("(?<" + matcher.group(2) + ">.+)");
            left = matcher.end();
        }
        if (left < patternStr.length()) {
            regexPattern.append(Pattern.quote(patternStr.substring(left)));
        }
        return regexPattern.toString();
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
                        spec(stepSpec.getDocString()).
                        build());
    }

    private String extractSpaceOffset(final String str, int maxOffset) {
        maxOffset = Math.min(str.length(), maxOffset);
        for (int i = 0; i < maxOffset; i++) {
            if (str.charAt(i) != ' ') {
                return str.substring(i);
            }
        }
        return str.substring(maxOffset);
    }
}
