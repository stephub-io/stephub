package org.mbok.cucumberform.runtime.service;

import lombok.*;
import org.mbok.cucumberform.json.Json;
import org.mbok.cucumberform.provider.StepRequest;
import org.mbok.cucumberform.provider.spec.StepSpec;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mbok.cucumberform.provider.spec.PatternType.REGEX;

@Service
public class PatternMatcher {
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

    public StepMatch match(StepSpec stepSpec, final String instruction) {
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
