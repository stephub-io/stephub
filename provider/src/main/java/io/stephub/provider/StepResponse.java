package io.stephub.provider;

import com.fasterxml.jackson.annotation.JsonValue;
import io.stephub.json.Json;
import io.stephub.json.JsonObject;
import lombok.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode
public class StepResponse {
    public enum StepStatus {
        PASSED, FAILED;

        @Override
        @JsonValue
        public String toString() {
            return this.name().toLowerCase();
        }
    }

    @AllArgsConstructor
    @Data
    @Builder
    @EqualsAndHashCode
    public static class Attachment {
        private String content;
        private String contentType;
    }

    @AllArgsConstructor
    @Data
    @Builder
    @EqualsAndHashCode
    public static class StepLog {
        private String message;
        private Attachment attachment;
        private JsonObject details;
    }

    private StepStatus status;
    private Duration duration;
    @Singular
    private Map<String, Json> outputs;
    private String errorMessage;
    @Singular
    private List<StepLog> logs;
}
