package io.stephub.provider;

import io.stephub.json.JsonObject;
import io.stephub.json.schema.JsonSchema;
import io.stephub.provider.spec.StepSpec;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Duration;
import java.util.List;

public interface Provider {
    String createSession(ProviderOptions options);

    StepResponse execute(String sessionId, StepRequest request);

    void destroySession(String sessionId);

    List<StepSpec> getSteps();

    String getName();

    JsonSchema getOptionsSchema();

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @SuperBuilder
    @EqualsAndHashCode
    class ProviderOptions {
        @Builder.Default
        private Duration sessionTimeout = Duration.ofMinutes(5);
        private JsonObject options;
    }
}
