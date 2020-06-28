package io.stephub.runtime.model.gherkin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Feature {
    private String name;

    @Valid
    private StepSequence background;

    @Singular
    @Valid
    private List<Scenario> scenarios = new ArrayList<>();
}
