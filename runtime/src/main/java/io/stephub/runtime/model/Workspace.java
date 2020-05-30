package io.stephub.runtime.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.stephub.runtime.model.customsteps.CustomStepContainer;
import io.stephub.runtime.model.customsteps.Step;
import io.stephub.runtime.validation.ProviderValidator;
import lombok.*;
import org.springframework.validation.ObjectError;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@EqualsAndHashCode
@ToString(of = {"id"})
public class Workspace implements CustomStepContainer {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String id;
    @NotEmpty
    private String name;

    @Builder.Default
    @Valid
    @NotNull
    private GherkinPreferences gherkinPreferences = new GherkinPreferences();

    @Valid
    @Singular
    private List<@ProviderValidator.Valid ProviderSpec> providers = new ArrayList<>();

    @Singular
    @Valid
    private List<Step> steps = new ArrayList<>();

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ObjectError> errors = null;

}
