package io.stephub.server.api.model.gherkin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.util.List;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class StepSequence {
    @NotNull
    private String name;

    @NotNull
    @Singular
    private List<String> steps;
}
