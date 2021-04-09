package io.stephub.server.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import io.stephub.server.api.model.gherkin.Feature;
import io.stephub.server.api.model.gherkin.Scenario;
import io.stephub.server.api.model.gherkin.StepSequence;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.stephub.server.api.model.gherkin.Feature.DEFAULT_TAG;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Data
public class Fixture extends StepSequence implements Identifiable {

    public boolean appliesTo(final Feature feature, final Scenario scenario) {
        return CollectionUtils.containsAny(feature.getTags(), this.activationTags)
                || CollectionUtils.containsAny(scenario.getTags(), this.activationTags);
    }

    public enum FixtureType {
        BEFORE, AFTER;

        @Override
        @JsonValue
        public String toString() {
            return this.name().toLowerCase();
        }
    }

    @NotNull
    private FixtureType type;

    @NotNull
    private String name;

    @NotNull
    @Builder.Default
    private List<String> activationTags = new ArrayList<>(Collections.singleton(DEFAULT_TAG));

    private int priority;

    private boolean abortOnError;

    @Override
    @JsonIgnore
    public String getId() {
        return this.type + "." + this.name;
    }
}
