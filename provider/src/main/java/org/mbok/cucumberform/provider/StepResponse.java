package org.mbok.cucumberform.provider;

import lombok.*;
import org.mbok.cucumberform.json.JsonObject;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

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
        private OffsetDateTime timestamp;
        private String message;
        private Attachment attachment;
        private JsonObject details;
    }

    private StepStatus status;
    private Duration duration;
    @Singular
    private List<Argument> outputs;
    private String errorMessage;
    private List<StepLog> logs;
}
