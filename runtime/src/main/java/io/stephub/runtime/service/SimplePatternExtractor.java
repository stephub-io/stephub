package io.stephub.runtime.service;

import io.stephub.json.Json;
import io.stephub.json.schema.JsonSchema;
import io.stephub.provider.api.model.spec.ArgumentSpec;
import io.stephub.runtime.model.GherkinPreferences;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SimplePatternExtractor {
    private static final Pattern SIMPLE_PATTERN_ARG_PATTERN = Pattern.compile("(^|[^\\\\])\\{([a-zA-Z_][a-zA-Z0-9_]*)\\}");
    public static final int DEFAULT_PATTERN_FLAGS = Pattern.CASE_INSENSITIVE;

    public Extraction extract(final GherkinPreferences gherkinPreferences, final String simplePattern, final boolean withOutput) {
        final Extraction.ExtractionBuilder extractionBuilder = Extraction.builder();
        final Matcher matcher = SIMPLE_PATTERN_ARG_PATTERN.matcher(simplePattern);
        int left = 0;
        final StringBuilder regexPattern = new StringBuilder();
        while (matcher.find()) {
            if (left < matcher.start()) {
                regexPattern.append(Pattern.quote(simplePattern.substring(left, matcher.start()) + matcher.group(1)));
            }
            regexPattern.append("(?<" + matcher.group(2) + ">.+?)");
            extractionBuilder.argument(
                    ArgumentSpec.<JsonSchema>builder().
                            name(matcher.group(2)).
                            schema(JsonSchema.ofType(Json.JsonType.ANY)).
                            build());
            left = matcher.end();
        }
        if (left < simplePattern.length()) {
            regexPattern.append(Pattern.quote(simplePattern.substring(left)));
        }
        extractionBuilder.regexPattern(Pattern.compile(gherkinPreferences.surround(regexPattern.toString(), withOutput), DEFAULT_PATTERN_FLAGS));
        return extractionBuilder.build();
    }

    @Builder
    @Getter
    public static class Extraction {
        private final Pattern regexPattern;
        @Singular
        private final List<ArgumentSpec<JsonSchema>> arguments;
    }
}
