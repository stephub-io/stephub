package io.stephub.runtime.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import io.stephub.provider.Provider.ProviderOptions;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public class ProviderSpec extends ProviderOptions  {
    @NotEmpty
    private String name;
    @NotEmpty
    @Pattern(regexp = "(>|>=|=|$)\\d+(\\.\\d+(\\.\\d+)?)?")
    private String version;

}
