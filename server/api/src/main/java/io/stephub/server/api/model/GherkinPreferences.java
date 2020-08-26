package io.stephub.server.api.model;


import lombok.*;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotEmpty;
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
            @NotEmpty
            @Pattern(regexp = ".*@ATTRIBUTE.*", message = "Must contain string '@ATTRIBUTE' as placeholder for attribute name content")
                    String> assignmentKeywords = CollectionUtils.arrayToList(new String[]{
            "- assigned to @ATTRIBUTE"
    });

    @Builder.Default
    @NotNull
    @Size(min = 1)
    private final List<@NotEmpty String> stepKeywords = CollectionUtils.arrayToList(new String[]{
            "Given", "When", "Then", "But"
    });

    public String surround(final String stepRegexPattern, final boolean withOutput) {
        final StringBuilder pattern = new StringBuilder();
        pattern.append("^\\s*(").append(
                this.stepKeywords.stream().map(java.util.regex.Pattern::quote).collect(Collectors.joining("|"))
        ).append(")\\s*").append(stepRegexPattern);
        if (withOutput) {
            pattern.append("(\\s*");
            pattern.append(
                    this.assignmentKeywords.stream().map(k -> {
                        final String[] parts = k.split("@ATTRIBUTE");
                        return java.util.regex.Pattern.quote(parts[0]) +
                                "(?<" + OUTPUT_ATTRIBUTE_GROUP_NAME + ">.+)" +
                                (parts.length > 1 ? java.util.regex.Pattern.quote(parts[1]) : "");
                    }).collect(Collectors.joining("|")));
            pattern.append(")?");
        }
        pattern.append("\\s*$");
        return pattern.toString();
    }
}
