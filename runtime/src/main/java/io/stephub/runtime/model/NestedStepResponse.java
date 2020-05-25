package io.stephub.runtime.model;

import io.stephub.json.Json;
import io.stephub.provider.api.model.StepResponse;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Duration;
import java.util.List;

@NoArgsConstructor
@SuperBuilder
@Getter
public class NestedStepResponse extends StepResponse<Json> {
    @Singular
    private List<Context> subResponses;

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class Context {
        private String name;
        @Singular
        private List<Entry> entries;
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class Entry {
        private String instruction;
        private StepResponse<Json> response;
    }

    @Override
    public StepStatus getStatus() {
        StepStatus status = super.getStatus();
        if (status == null) {
            status = StepStatus.PASSED;
            ML:
            for (final Context context : this.subResponses) {
                for (final Entry entry : context.entries) {
                    final StepStatus subStatus = entry.response.getStatus();
                    if (subStatus != null && subStatus != StepStatus.PASSED) {
                        status = subStatus;
                        break ML;
                    }
                }
            }
        }
        return status;
    }

    @Override
    public Duration getDuration() {
        Duration duation = super.getDuration();
        if (duation == null) {
            duation = Duration.ofMillis(0);
            for (final Context context : this.subResponses) {
                for (final Entry entry : context.entries) {
                    final Duration subDuration = entry.response.getDuration();
                    if (subDuration != null) {
                        duation = duation.plus(subDuration);
                    }
                }
            }
        }
        return duation;
    }

    @Override
    public String getErrorMessage() {
        return super.getErrorMessage();
    }
}
