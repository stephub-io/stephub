package io.stephub.runtime.model;


import io.stephub.runtime.validation.RegexValidator;
import lombok.*;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode
@Builder
public class GherkinPreferences {
    public static final String OUTPUT_ATTRIBUTE_GROUP_NAME = "output";

    @Builder.Default
    @NotNull
    @Size(min = 1)
    private final List<
            @RegexValidator.Regex
            @Pattern(regexp = ".*@ATTRIBUTE.*", message = "Must contain string '@ATTRIBUTE' as placeholder for attribute name content")
                    String> stepOutputAssignmentSuffixes = CollectionUtils.arrayToList(new String[]{
            "- assigned to @ATTRIBUTE"
    });

    @Builder.Default
    @NotNull
    @Size(min = 1)
    private final List<@RegexValidator.Regex String> stepPrefixes = CollectionUtils.arrayToList(new String[]{
            "Given( I)?", "When( I)?", "Then( I)?", "But( I)?"
    });

    public String surround(final String stepRegexPattern, final boolean withOutput) {
        final StringBuilder pattern = new StringBuilder();
        pattern.append("^\\s*(").append(
                this.stepPrefixes.stream().map(p -> "(" + p + ")").collect(Collectors.joining("|"))
        ).append(")\\s*").append(stepRegexPattern);
        if (withOutput) {
            pattern.append("(\\s*");
            pattern.append(
                    this.stepOutputAssignmentSuffixes.stream().map(p -> "(" +
                            p.replaceFirst("@ATTRIBUTE", "(?<" + OUTPUT_ATTRIBUTE_GROUP_NAME + ">.+)")
                            + ")").collect(Collectors.joining("|")));
            pattern.append(")?");
        }
        pattern.append("\\s*$");
        return pattern.toString();
    }
}
