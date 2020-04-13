package org.mbok.cucumberform.runtime.service.support;

import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpiringMap;
import org.mbok.cucumberform.expression.AttributesContext;
import org.mbok.cucumberform.json.Json;
import org.mbok.cucumberform.provider.StepResponse;
import org.mbok.cucumberform.runtime.model.Context;
import org.mbok.cucumberform.runtime.model.RuntimeSession;
import org.mbok.cucumberform.runtime.model.StepExecution;
import org.mbok.cucumberform.runtime.model.Workspace;
import org.mbok.cucumberform.runtime.service.ExecutionException;
import org.mbok.cucumberform.runtime.service.ProvidersFacade;
import org.mbok.cucumberform.runtime.service.ResourceNotFoundException;
import org.mbok.cucumberform.runtime.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.mbok.cucumberform.runtime.model.RuntimeSession.SessionStatus.ACTIVE;
import static org.mbok.cucumberform.runtime.model.RuntimeSession.SessionStatus.INACTIVE;

@Service
@Slf4j
public class MemorySessionService implements SessionService {
    @Autowired
    private ProvidersFacade providersFacade;

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
    public RuntimeSession startSession(final Context ctx, final Workspace workspace) {
        final RuntimeSession session = RuntimeSession.builder().id(UUID.randomUUID().toString()).
                workspace(workspace).
                status(ACTIVE).
                build();
        this.sessionStore.put(session.getId(), session);
        log.info("Started session={}", session);
        return session;
    }

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
    public StepResponse execute(final Context ctx, final String wid, final String sid, final StepExecution stepExecution) {
        final RuntimeSession session = this.getSessionSafe(wid, sid);
        if (session.getStatus() == INACTIVE) {
            throw new ExecutionException("Session isn't active with id=" + sid);
        }
        log.debug("Execute within session={} the step={}", session, stepExecution);
        return this.providersFacade.execute(session.getWorkspace(), stepExecution, new ProvidersFacade.ProviderSessionStore() {
            @Override
            public String getProviderSession(final String providerName) {
                return session.getProviderSessions().get(providerName);
            }

            @Override
            public void setProviderSession(final String providerName, final String providerSession) {
                session.getProviderSessions().put(providerName, providerSession);
            }
        }, new AttributesContext() {
            @Override
            public Json get(final String key) {
                return session.getGlobals().get(key);
            }
        });
    }

    private RuntimeSession getSessionSafe(final String wid, final String sid) {
        final RuntimeSession session = this.sessionStore.get(sid);
        if (session == null || !session.getWorkspace().getId().equals(wid)) {
            throw new ResourceNotFoundException("Session doesn't exist or is invalid for id=" + sid + " and workspace=" + wid);
        }
        return session;
    }
}
