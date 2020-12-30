package io.stephub.server.api.model.gherkin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.stephub.server.api.model.Identifiable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@SuperBuilder
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class Scenario extends StepSequence implements Tagable, Identifiable {
    @NotNull
    private String name;

    @Singular
    private List<String> tags = new ArrayList<>();

    @Singular
    private List<String> comments = new ArrayList<>();


    @Override
    @JsonIgnore
    public String getId() {
        return this.getName();
    }
}
