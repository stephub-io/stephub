package io.stephub.provider;

import lombok.*;
import lombok.experimental.SuperBuilder;
import io.stephub.json.JsonObject;
import io.stephub.provider.spec.StepSpec;

import java.time.Duration;
import java.util.List;

public interface Provider {
    String createSession(ProviderOptions options);

    StepResponse execute(String sessionId, StepRequest request);

    void destroySession(String sessionId);

    List<StepSpec> getSteps();

    String getName();

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
