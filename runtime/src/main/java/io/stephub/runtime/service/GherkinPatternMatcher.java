package io.stephub.runtime.service;

import io.stephub.expression.ParseException;
import io.stephub.json.Json;
import io.stephub.provider.spec.StepSpec;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.stephub.provider.spec.PatternType.REGEX;
import static io.stephub.provider.spec.StepSpec.PayloadType.DOC_STRING;

@Service
public class GherkinPatternMatcher {
    private static final Pattern DOC_STRING_MARKER = Pattern.compile("(\\s*)\"\"\"\\s*");

    @Getter
    @Builder
    public static class StepMatch {
        @Singular
        private final List<ArgumentMatch> arguments;
        private final String docString;
    }

    @Getter
    @Builder
    public static class ArgumentMatch {
        private final String name;
        private final String value;
        private final Json.JsonType desiredType;
    }

    public StepMatch matches(final StepSpec stepSpec, final String instruction) {
        if (stepSpec.getPatternType() == REGEX) {
            final Pattern pattern = Pattern.compile(stepSpec.getPattern());
            final String[] lines = instruction.split("\n");
            final Matcher matcher = pattern.matcher(lines[0]);
            if (matcher.matches()) {
                final StepMatch.StepMatchBuilder stepMatchBuilder = StepMatch.builder();
                stepSpec.getArguments().forEach(a -> stepMatchBuilder.argument(
                        ArgumentMatch.builder().
                                name(a.getName()).
                                value(matcher.group(a.getName())).
                                desiredType(a.getType()).build()
                ));
                this.checkAndExtractPayload(stepSpec, instruction, lines, stepMatchBuilder);
                return stepMatchBuilder.build();
            } else {
                return null;
            }
        }
        throw new UnsupportedOperationException("Pattern matching not implemented for type=" + stepSpec.getPatternType());
    }

    private void checkAndExtractPayload(final StepSpec stepSpec, final String instruction, final String[] lines, final StepMatch.StepMatchBuilder stepMatchBuilder) {
        if (stepSpec.getPayload() == DOC_STRING) {
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
            stepMatchBuilder.docString(extraction.toString());
        }
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