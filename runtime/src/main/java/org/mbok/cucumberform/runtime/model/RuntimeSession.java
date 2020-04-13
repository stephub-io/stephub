package org.mbok.cucumberform.runtime.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;
import org.mbok.cucumberform.json.Json;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(of = {"id", "workspace"})
@Builder
public class RuntimeSession {
    public static enum SessionStatus {
        ACTIVE, INACTIVE;

        @JsonValue
        public String forJackson() {
            return this.name().toLowerCase();
        }
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String id;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Workspace workspace;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private SessionStatus status;

    private Map<String, Json> globals = new HashMap<>();

    @JsonIgnore
    private Map<String, String> providerSessions = new HashMap<>();
}
