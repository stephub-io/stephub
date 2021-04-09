package io.stephub.server.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.stephub.provider.api.util.Patterns;
import io.stephub.server.api.model.customsteps.CustomStepContainer;
import io.stephub.server.api.model.customsteps.StepDefinition;
import io.stephub.server.api.model.gherkin.Feature;
import io.stephub.server.api.validation.ValidProviderSpec;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.*;


@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
@EqualsAndHashCode
@ToString(of = {"id"})
public class Workspace implements CustomStepContainer, Identifiable {
    private String id;
    @NotEmpty
    private String name;

    @Builder.Default
    @Valid
    @NotNull
    private GherkinPreferences gherkinPreferences = new GherkinPreferences();

    @Valid
    @Builder.Default
    private List<@ValidProviderSpec ProviderSpec> providers = new ArrayList<>(
            Collections.singletonList(
                    ProviderSpec.builder().name("base").build()
            )
    );

    @Valid
    @Builder.Default
    private List<StepDefinition> stepDefinitions = new ArrayList<>();

    @Valid
    @Builder.Default
    private Map<@Pattern(regexp = Patterns.ID_PATTERN_STR) String, Variable> variables = new HashMap<>();

    @Valid
    @Builder.Default
    private List<Feature> features = new ArrayList<>();

    @Valid
    @Builder.Default
    private List<Fixture> fixtures = new ArrayList<>();

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<FieldError> errors = null;

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    @Data
    public static class FieldError {
        private String field;
        private Object rejectedValue;
        private String message;
    }

}
