package io.stephub.provider;

import lombok.*;
import io.stephub.json.JsonObject;

import java.time.Duration;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode
public class StepResponse {
    public enum StepStatus {
        PASSED, FAILED
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
    private List<Argument> outputs;
    private String errorMessage;
    @Singular
    private List<StepLog> logs;
}
