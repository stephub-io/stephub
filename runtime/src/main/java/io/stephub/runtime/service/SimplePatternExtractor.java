package io.stephub.runtime.service;

import io.stephub.json.Json;
import io.stephub.json.schema.JsonSchema;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SimplePatternExtractor {
    private static final Pattern SIMPLE_PATTERN_ARG_PATTERN = Pattern.compile("(^|[^\\\\])\\{([a-zA-Z_][a-zA-Z0-9_]*)\\}");

    public Extraction extract(String simplePattern) {
        Extraction.ExtractionBuilder extractionBuilder = Extraction.builder();
        final Matcher matcher = SIMPLE_PATTERN_ARG_PATTERN.matcher(simplePattern);
        int left = 0;
        final StringBuilder regexPattern = new StringBuilder();
        while (matcher.find()) {
            if (left < matcher.start()) {
                regexPattern.append(Pattern.quote(simplePattern.substring(left, matcher.start()) + matcher.group(1)));
            }
            regexPattern.append("(?<" + matcher.group(2) + ">.+)");
            extractionBuilder.argument(matcher.group(2), JsonSchema.ofType(Json.JsonType.JSON));
            left = matcher.end();
        }
        if (left < simplePattern.length()) {
            regexPattern.append(Pattern.quote(simplePattern.substring(left)));
        }
        extractionBuilder.regexPattern(Pattern.compile(regexPattern.toString()));
        return extractionBuilder.build();
    }

    @Builder
    @Getter
    public static class Extraction {
        private Pattern regexPattern;
        @Singular
        private Map<String, JsonSchema> arguments;
    }
}
