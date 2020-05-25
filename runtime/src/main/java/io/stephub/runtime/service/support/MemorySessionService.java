package io.stephub.runtime.service.support;

import io.stephub.runtime.model.Context;
import io.stephub.runtime.model.RuntimeSession;
import io.stephub.runtime.model.Workspace;
import io.stephub.runtime.service.*;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static io.stephub.runtime.model.RuntimeSession.SessionStatus.ACTIVE;
import static io.stephub.runtime.model.RuntimeSession.SessionStatus.INACTIVE;

@Service
@Slf4j
public class MemorySessionService extends SessionService {
    @Autowired
    private ProvidersFacade providersFacade;

    @Autowired
    private WorkspaceService workspaceService;

    @Autowired
    private WorkspaceValidator workspaceValidator;

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
    public RuntimeSession startSession(final Context ctx, final String wid) {
        final Workspace workspace = this.workspaceService.getWorkspace(ctx, wid);
        this.workspaceValidator.validate(workspace);
        if (workspace.getErrors() != null && !workspace.getErrors().isEmpty()) {
            throw new ExecutionException("Erroneous workspace, please correct the errors first");
        }
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
    public <T> T executeWithinSession(final Context ctx, final String wid, final String sid, final WinthinSessionExecutor<T> executor) {
        final RuntimeSession session = this.getSessionSafe(wid, sid);
        return executor.execute(session, new SessionExecutionContext() {
            @Override
            public void setProviderSession(final String providerName, final String sid) {
                session.getProviderSessions().put(providerName, sid);
            }

            @Override
            public String getProviderSession(final String providerName) {
                return session.getProviderSessions().get(providerName);
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
