package io.stephub.server.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.stephub.json.Json;
import io.stephub.provider.api.util.Patterns;
import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(of = {"id"})
@ToString(of = {"id", "status"})
@Builder
public class RuntimeSession implements Identifiable {
    public enum SessionStatus {
        ACTIVE,
        INACTIVE;

        @Override
        @JsonValue
        public String toString() {
            return this.name().toLowerCase();
        }
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String id;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private SessionStatus status;

    @Builder.Default
    private Map<String, Json> attributes = new HashMap<>();

    @JsonIgnore
    @Builder.Default
    private Map<String, String> providerSessions = new HashMap<>();

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Data
    public static class SessionSettings {
        @NotNull
        @Builder.Default
        private Map<@Pattern(regexp = Patterns.ID_PATTERN_STR) String, Json> variables = new HashMap<>();

        public static enum ParallelizationMode {
            FEATURE, SCENARIO;

            @Override
            @JsonValue
            public String toString() {
                return this.name().toLowerCase();
            }
        }

        @Builder.Default
        private ParallelizationMode parallelizationMode = ParallelizationMode.SCENARIO;
    }
}
