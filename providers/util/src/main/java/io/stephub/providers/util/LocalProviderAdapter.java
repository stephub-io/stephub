package io.stephub.providers.util;

import io.stephub.provider.*;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class LocalProviderAdapter<S extends LocalProviderAdapter.SessionState> implements Provider {

    private final ExpiringMap<String, S> sessionStore = ExpiringMap.builder()
            .expirationListener((sessionId, state) -> {
                log.info("Session expired: {}", sessionId);
                this.stopState((S) state);
            })
            .variableExpiration()
            .build();

    @Data
    @SuperBuilder
    public static class SessionState {
        private String sessionId;
        private ProviderOptions providerOptions;
    }

    @Override
    public final String createSession(final ProviderOptions options) {
        final String sessionId = UUID.randomUUID().toString();
        final S state = this.startState(sessionId, options);
        state.setSessionId(sessionId);
        state.setProviderOptions(options);
        this.sessionStore.put(sessionId, state, ExpirationPolicy.ACCESSED, options.getSessionTimeout().getSeconds(), TimeUnit.SECONDS);
        log.debug("Created session {} with options: {}", sessionId, options);
        return sessionId;
    }

    protected abstract S startState(String sessionId, ProviderOptions options);

    protected abstract void stopState(S state);

    protected abstract StepResponse executeWithinState(String sessionId, S state, StepRequest request);

    @Override
    public final StepResponse execute(final String sessionId, final StepRequest request) {
        return this.executeWithinState(sessionId, this.getStateSafe(sessionId), request);
    }

    @Override
    public final void destroySession(final String sessionId) {
        this.stopState(this.getStateSafe(sessionId));
        this.sessionStore.remove(sessionId);
        log.debug("Destroyed session: {}", sessionId);
    }

    private S getStateSafe(final String sessionId) {
        final S state = this.sessionStore.get(sessionId);
        if (state == null) {
            throw new ProviderException("Session not found or expired: " + sessionId);
        }
        return state;
    }
}
