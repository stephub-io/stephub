package io.stephub.runtime.service;

import lombok.*;
import io.stephub.json.Json;
import io.stephub.provider.spec.StepSpec;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.stephub.provider.spec.PatternType.REGEX;

@Service
public class GherkinPatternMatcher {
    @Getter
    @Builder
    public static class StepMatch {
        @Singular
        private List<ArgumentMatch> arguments;
    }

    @Getter
    @Builder
    public static class ArgumentMatch {
        private String name;
        private String value;
        private Json.JsonType desiredType;
    }

    public StepMatch matches(StepSpec stepSpec, final String instruction) {
        if (stepSpec.getPatternType() == REGEX) {
            Pattern pattern = Pattern.compile(stepSpec.getPattern());
            Matcher matcher = pattern.matcher(instruction);
            if (matcher.matches()) {
                final StepMatch.StepMatchBuilder stepMatchBuilder = StepMatch.builder();
                stepSpec.getArguments().forEach(a -> stepMatchBuilder.argument(
                        ArgumentMatch.builder().
                                name(a.getName()).
                                value(matcher.group(a.getName())).
                                desiredType(a.getType()).build()
                ));
                return stepMatchBuilder.build();
            } else {
                return null;
            }
        }
        throw new UnsupportedOperationException("Pattern matching not implemented for type=" + this);
    }
}
