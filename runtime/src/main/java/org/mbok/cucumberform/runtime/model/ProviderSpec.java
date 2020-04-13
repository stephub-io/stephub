package org.mbok.cucumberform.runtime.model;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode
@ToString
@Builder
public class ProviderSpec {
    @NotEmpty
    private String name;
    @NotEmpty
    @Pattern(regexp = "(>|>=|=|$)\\d+(\\.\\d+(\\.\\d+)?)?")
    private String version;
}
