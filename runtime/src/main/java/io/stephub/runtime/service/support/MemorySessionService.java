package io.stephub.runtime.service.support;

import io.stephub.expression.AttributesContext;
import io.stephub.json.Json;
import io.stephub.runtime.model.Context;
import io.stephub.runtime.model.RuntimeSession;
import io.stephub.runtime.model.RuntimeSession.SessionSettings;
import io.stephub.runtime.model.Workspace;
import io.stephub.runtime.service.ResourceNotFoundException;
import io.stephub.runtime.service.SessionExecutionContext;
import io.stephub.runtime.service.SessionService;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static io.stephub.runtime.model.RuntimeSession.SessionStatus.ACTIVE;
import static io.stephub.runtime.model.RuntimeSession.SessionStatus.INACTIVE;

@Service
@Slf4j
public class MemorySessionService extends SessionService {

    private final ExpiringMap<String, RuntimeSession> sessionStore = ExpiringMap.builder()
            .expirationListener((sessionId, s) -> {
                RuntimeSession session = (RuntimeSession) s;
                if (session.getStatus() == ACTIVE) {
                    this.stopSession(session);
                }
                log.info("Removed session from store: {}", sessionId);
            })
            .expiration(5, TimeUnit.MINUTES)
            .build();

    @Override
    public List<RuntimeSession> getSessions(final Context ctx, final String wid) {
        return new ArrayList<>(this.sessionStore.values());
    }

    @Override
    public RuntimeSession startSession(final Workspace workspace, final SessionSettings sessionSettings, final Map<String, Json> attributes) {
        final RuntimeSession session = RuntimeSession.builder().id(UUID.randomUUID().toString()).
                workspace(workspace).
                status(ACTIVE).
                attributes(attributes).
                build();
        this.sessionStore.put(session.getId(), session);
        log.info("Started session={}", session);
        return session;
    }

    @Override
    public void stopSession(final RuntimeSession session) {
        log.info("Stopping session={}", session);
        session.setStatus(INACTIVE);
    }

    @Override
    public void stopSession(final Context ctx, final String wid, final String sid) {
        this.stopSession(this.getSessionSafe(wid, sid));
    }

    @Override
    public RuntimeSession getSession(final Context ctx, final String wid, final String sid) {
        return this.getSessionSafe(wid, sid);
    }

    @Override
    public void executeWithinSessionInternal(final String wid, final String sid, final WithinSessionExecutorInternal executor) {
        final RuntimeSession session = this.getSessionSafe(wid, sid);
        executor.execute(session,
                new SessionExecutionContext() {
                    @Override
                    public void setProviderSession(final String providerName, final String sid) {
                        session.getProviderSessions().put(providerName, sid);
                    }

                    @Override
                    public String getProviderSession(final String providerName) {
                        return session.getProviderSessions().get(providerName);
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

    private RuntimeSession getSessionSafe(final String wid, final String sid) {
        final RuntimeSession session = this.sessionStore.get(sid);
        if (session == null || !session.getWorkspace().getId().equals(wid)) {
            throw new ResourceNotFoundException("Session doesn't exist or is invalid for id=" + sid + " and workspace=" + wid);
        }
        return session;
    }
}
