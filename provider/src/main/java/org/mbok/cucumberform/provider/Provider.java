package org.mbok.cucumberform.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.mbok.cucumberform.json.JsonObject;

import java.time.Duration;

public interface Provider {
    String createSession(ProviderOptions options);
    StepResponse execute(String sessionId, StepRequest request);
    void destroySession(String sessionId);

    @AllArgsConstructor
    @Data
    @Builder
    @EqualsAndHashCode
    class ProviderOptions {
        private Duration sessionTimeout = Duration.ofMinutes(5);
        private JsonObject options;
    }
}
