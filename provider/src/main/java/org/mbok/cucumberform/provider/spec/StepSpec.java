package org.mbok.cucumberform.provider.spec;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@EqualsAndHashCode
@ToString
public class StepSpec {
    private String pattern;
    private PatternType patternType;
    @Singular
    private List<ArgumentSpec> arguments;
}
