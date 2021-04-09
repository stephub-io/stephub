package io.stephub.server.service.support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.stephub.expression.AttributesContext;
import io.stephub.json.Json;
import io.stephub.server.api.SessionExecutionContext;
import io.stephub.server.api.model.ProviderSpec;
import io.stephub.server.api.model.RuntimeSession;
import io.stephub.server.api.model.RuntimeSession.SessionSettings;
import io.stephub.server.api.model.Workspace;
import io.stephub.server.model.Context;
import io.stephub.server.service.ResourceNotFoundException;
import io.stephub.server.service.SessionService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.stephub.server.api.model.RuntimeSession.SessionStatus.ACTIVE;
import static io.stephub.server.api.model.RuntimeSession.SessionStatus.INACTIVE;

@Service
@Slf4j
public class MemorySessionService extends SessionService {

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @SuperBuilder
    public static class MemoryRuntimeSession extends RuntimeSession {
        @JsonIgnore
        @Builder.Default
        private Map<ProviderSpec, String> providerSessions = new HashMap<>();

    }

    private final ExpiringMap<String, MemoryRuntimeSession> sessionStore = ExpiringMap.builder()
            .expirationListener((sessionId, s) -> {
                RuntimeSession session = (MemoryRuntimeSession) s;
                if (session.getStatus() == ACTIVE) {
                    this.stopSession(session);
                }
                log.info("Removed session from store: {}", sessionId);
            })
            .expiration(5, TimeUnit.MINUTES)
            .build();

    @Override
    public List<RuntimeSession> getSessions(final Context ctx, final String wid) {
        return this.sessionStore.entrySet().stream().
                filter(entry -> entry.getKey().startsWith(wid + "/")).map(Map.Entry::getValue).
                collect(Collectors.toList());
    }

    @Override
    public RuntimeSession startSession(final Workspace workspace, final SessionSettings sessionSettings, final Map<String, Json> attributes) {
        final MemoryRuntimeSession session = MemoryRuntimeSession.builder().id(UUID.randomUUID().toString()).
                status(ACTIVE).
                attributes(attributes).
                build();
        this.sessionStore.put(workspace.getId() + "/" + session.getId(), session);
        log.info("Started session={}", session);
        return session;
    }

    @Override
    public void deactivateSession(final RuntimeSession session) {
        log.debug("Deactivated session={}", session);
        session.setStatus(INACTIVE);
        this.sessionStore.remove(session.getId());
    }

    @Override
    protected Map<ProviderSpec, String> getProviderSessions(final RuntimeSession session) {
        return ((MemoryRuntimeSession) session).getProviderSessions();
    }

    @Override
    public RuntimeSession getSession(final Context ctx, final String wid, final String sid) {
        return this.getSessionSafe(wid, sid);
    }

    @Override
    public void executeWithinSessionInternal(final String wid, final String sid, final Map<String, Json> presetAttributes, final WithinSessionExecutorInternal executor) {
        final MemoryRuntimeSession session = this.getSessionSafe(wid, sid);
        if (presetAttributes != null) {
            session.getAttributes().putAll(presetAttributes);
        }
        executor.execute(session,
                new SessionExecutionContext() {
                    @Override
                    public void setProviderSession(final ProviderSpec providerSpec, final String sid) {
                        session.getProviderSessions().put(providerSpec, sid);
                    }

                    @Override
                    public String getProviderSession(final ProviderSpec providerSpec) {
                        return session.getProviderSessions().get(providerSpec);
                    }
                },
                new AttributesContext() {
                    @Override
                    public Json get(final String key) {
                        return session.getAttributes().get(key);
                    }

                    @Override
                    public void put(final String key, final Json value) {
                        session.getAttributes().put(key, value);
                    }
                }
        );
    }

    private MemoryRuntimeSession getSessionSafe(final String wid, final String sid) {
        final MemoryRuntimeSession session = this.sessionStore.get(wid + "/" + sid);
        if (session == null) {
            throw new ResourceNotFoundException("Session doesn't exist or is invalid for id=" + sid + " and workspace=" + wid);
        }
        return session;
    }
}
