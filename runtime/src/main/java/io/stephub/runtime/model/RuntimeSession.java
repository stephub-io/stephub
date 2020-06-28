package io.stephub.runtime.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.stephub.json.Json;
import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.HashMap;
import java.util.Map;

import static io.stephub.provider.api.util.Patterns.ID_PATTERN_STR;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(of = {"id", "workspace"})
@ToString(of = {"id", "status"})
@Builder
public class RuntimeSession {
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
    private Workspace workspace;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private SessionStatus status;

    @Builder.Default
    private Map<String, Json> attributes = new HashMap<>();

    @JsonIgnore
    @Builder.Default
    private Map<String, String> providerSessions = new HashMap<>();

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class SessionSettings {
        @NotNull
        private Map<@Pattern(regexp = ID_PATTERN_STR) String, Json> variables = new HashMap<>();
    }
}
