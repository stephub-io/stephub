package io.stephub.server.api.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;
import io.stephub.json.Json;
import io.stephub.provider.api.model.LogEntry;
import io.stephub.provider.api.model.StepResponse;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.util.Date;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
@EqualsAndHashCode(of = "id")
@ToString(of = "id")
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FunctionalExecution.class, name = Execution.FUNCTIONAL_STR),
        @JsonSubTypes.Type(value = LoadExecution.class, name = Execution.LOAD_STR)
})
public abstract class Execution {
    public final static String FUNCTIONAL_STR = "functional";
    public final static String LOAD_STR = "load";

    public enum ExecutionType {
        FUNCTIONAL(FunctionalExecution.class), LOAD(LoadExecution.class);

        private final Class<Execution> type;

        ExecutionType(final Class<? extends Execution> type) {
            this.type = (Class<Execution>) type;
        }

        public Class<Execution> getType() {
            return this.type;
        }

        @Override
        @JsonValue
        public String toString() {
            return this.name().toLowerCase();
        }
    }

    public enum ExecutionStatus {
        INITIATED, EXECUTING, COMPLETED, CANCELLED;

        @Override
        @JsonValue
        public String toString() {
            return this.name().toLowerCase();
        }
    }

    private String id;
    @Builder.Default
    private ExecutionStatus status = ExecutionStatus.INITIATED;

    @JsonFormat(shape = STRING)
    private Date initiatedAt;
    @JsonFormat(shape = STRING)
    private Date startedAt;
    @JsonFormat(shape = STRING)
    private Date completedAt;

    private boolean erroneous;
    private String errorMessage;


    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class RoughStepResponse {
        @NotNull
        private StepResponse.StepStatus status;

        @NotNull
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private Duration duration;

        private String errorMessage;

        @Valid
        private Json output;

        private List<ExecutionLogEntry> logs;

        public RoughStepResponse(final StepResponse<Json> from, final List<ExecutionLogEntry> logs) {
            this.status = from.getStatus();
            this.duration = from.getDuration();
            this.errorMessage = from.getErrorMessage();
            this.output = from.getOutput();
            this.logs = logs;
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static class ExecutionLogEntry {
        private String message;
        @Singular
        private List<ExecutionLogAttachment> attachments;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static class ExecutionLogAttachment {
        private String id;
        private String fileName;
        private String contentType;
        private long size;

        public ExecutionLogAttachment(final String id, final LogEntry.LogAttachment attachment) {
            this.id = id;
            this.fileName = attachment.getFileName();
            this.contentType = attachment.getContentType();
            this.size = attachment.getContent().length;
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Stats {
        int passed;
        int failed;
        int erroneous;

        void add(final Stats stats) {
            this.passed += stats.passed;
            this.failed += stats.failed;
            this.erroneous += stats.erroneous;
        }
    }

    @NoArgsConstructor
    @Data
    @SuperBuilder
    @AllArgsConstructor
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = FunctionalExecution.FunctionalExecutionStart.class, name = Execution.FUNCTIONAL_STR),
            @JsonSubTypes.Type(value = LoadExecution.LoadExecutionStart.class, name = Execution.LOAD_STR)
    })
    public abstract static class ExecutionStart<E extends Execution> {
        @Valid
        @Builder.Default
        private RuntimeSession.SessionSettings sessionSettings = new RuntimeSession.SessionSettings();

    }
}
