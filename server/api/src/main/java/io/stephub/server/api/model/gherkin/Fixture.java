package io.stephub.server.api.model.gherkin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.stephub.server.api.model.Identifiable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Data
public class Fixture extends StepSequence implements Identifiable {
    @NotNull
    private String name;

    private int priority;


    @Override
    @JsonIgnore
    public String getId() {
        return this.getName();
    }
}
