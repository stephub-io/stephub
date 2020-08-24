package io.stephub.server.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.stephub.provider.api.util.Patterns;
import io.stephub.server.api.model.customsteps.CustomStepContainer;
import io.stephub.server.api.model.customsteps.StepDefinition;
import io.stephub.server.api.model.gherkin.Feature;
import io.stephub.server.api.model.gherkin.Fixture;
import io.stephub.server.api.validation.ValidProviderSpec;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.validation.ObjectError;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
    @Singular
    private List<@ValidProviderSpec ProviderSpec> providers = new ArrayList<>();

    @Singular
    @Valid
    private List<StepDefinition> stepDefinitions = new ArrayList<>();

    @Singular
    @Valid
    private Map<@Pattern(regexp = Patterns.ID_PATTERN_STR) String, Variable> variables = new HashMap<>();

    @Singular
    @Valid
    private List<Feature> features = new ArrayList<>();

    @Singular
    @Valid
    private List<Fixture> beforeFixtures = new ArrayList<>();

    @Singular
    @Valid
    private List<Fixture> afterFixtures = new ArrayList<>();

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ObjectError> errors = null;

}
