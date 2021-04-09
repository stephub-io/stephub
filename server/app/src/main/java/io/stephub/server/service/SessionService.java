package io.stephub.server.service;

import io.stephub.expression.AttributesContext;
import io.stephub.expression.EvaluationContext;
import io.stephub.expression.FunctionFactory;
import io.stephub.json.Json;
import io.stephub.json.JsonObject;
import io.stephub.server.api.SessionExecutionContext;
import io.stephub.server.api.model.ProviderSpec;
import io.stephub.server.api.model.RuntimeSession;
import io.stephub.server.api.model.Workspace;
import io.stephub.server.model.Context;
import io.stephub.server.service.exception.ExecutionException;
import io.stephub.server.service.exception.ExecutionPrerequisiteException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class SessionService {

    @Autowired
    private FunctionFactory functionFactory;

    @Autowired
    protected WorkspaceService workspaceService;

    @Autowired
    private WorkspaceValidator workspaceValidator;

    @Autowired
    private ProvidersFacade providersFacade;

    public abstract List<RuntimeSession> getSessions(Context ctx, String wid);

    public final RuntimeSession startSession(final Context ctx, final String wid, final RuntimeSession.SessionSettings sessionSettings) {
        final Workspace workspace = this.workspaceService.getWorkspace(ctx, wid);
        return this.startSession(workspace, sessionSettings);
    }

    public final RuntimeSession startSession(final Workspace workspace, final RuntimeSession.SessionSettings sessionSettings) {
        this.workspaceValidator.validate(workspace);
        if (workspace.getErrors() != null && !workspace.getErrors().isEmpty()) {
            throw new ExecutionException("Erroneous workspace, please correct the errors first");
        }
        final Map<String, Json> attributes = this.setUpAttributes(workspace, sessionSettings, new VariableBindingRejector() {
            private final BindException bindException = new BindException(sessionSettings, "sessionSettings");

            @Override
            public void reject(final String key, final String message) {
                this.bindException.rejectValue("variables[" + key + "]", null, message);
            }

            @Override
            public boolean hasErrors() {
                return this.bindException.hasErrors();
            }

            @Override
            public Exception buildException() {
                return this.bindException;
            }
        });
        return this.startSession(workspace, sessionSettings, attributes);
    }

    protected abstract RuntimeSession startSession(Workspace workspace, RuntimeSession.SessionSettings sessionSettings, Map<String, Json> attributes);

    public final void stopSession(final Context ctx, final String wid, final String sid) {
        this.stopSession(this.getSession(ctx, wid, sid));
    }

    public abstract void deactivateSession(RuntimeSession session);

    protected abstract Map<ProviderSpec, String> getProviderSessions(RuntimeSession session);

    public final void stopSession(final RuntimeSession session) {
        log.info("Stopping session={}", session);
        this.getProviderSessions(session).entrySet().forEach(
                entry -> {
                    final String pid = entry.getValue();
                    final ProviderSpec providerSpec = entry.getKey();
                    log.debug("Stopping provider's '{}' session: {}", providerSpec.getId(), pid);
                    try {
                        this.providersFacade.getProvider(providerSpec).destroySession(pid);
                    } catch (final Exception e) {
                        log.warn("Failed to destroy session for provider '" + providerSpec.getId() + "': " + pid, e);
                    }
                }
        );
        this.deactivateSession(session);
        log.info("Session stopped {}", session.getId());
    }

    public abstract RuntimeSession getSession(Context ctx, String wid, String sid);

    Map<String, Json> setUpAttributes(final Workspace workspace, final RuntimeSession.SessionSettings sessionSettings, final VariableBindingRejector variableBindingRejector) {
        final Map<String, Json> attributes = new HashMap<>();
        final JsonObject vars = new JsonObject();
        final BindException varValidException = new BindException(sessionSettings, "sessionSettings");
        workspace.getVariables().forEach((key, var) -> {
            Json value = sessionSettings.getVariables().get(key);
            if (value == null) {
                value = var.getDefaultValue();
            }
            try {
                value = var.getSchema().convertFrom(value);
                var.getSchema().accept(value);
            } catch (final Exception e) {
                variableBindingRejector.reject(key, "Invalid value for variable '" + key + "': " + e.getMessage());
            }
            vars.getFields().put(key, value);
        });
        if (variableBindingRejector.hasErrors()) {
            throw new ExecutionPrerequisiteException("Invalid variable values", variableBindingRejector.buildException());
        }
        attributes.put("var", vars);
        return attributes;
    }

    public interface WithinSessionExecutor {
        void execute(RuntimeSession session, SessionExecutionContext sessionExecutionContext, EvaluationContext evaluationContext);
    }

    protected interface WithinSessionExecutorInternal {
        void execute(RuntimeSession session, SessionExecutionContext sessionExecutionContext, AttributesContext attributesContext);
    }

    protected void executeWithinSession(final String wid, final String sid, final Map<String, Json> presetAttributes, final WithinSessionExecutor executor) {
        this.executeWithinSessionInternal(wid, sid, presetAttributes, (workspace, sessionExecutionContext, attributesContext) ->
                executor.execute(workspace, sessionExecutionContext, new EvaluationContext() {
                    @Override
                    public Json get(final String key) {
                        return attributesContext.get(key);
                    }

                    @Override
                    public void put(final String key, final Json value) {
                        attributesContext.put(key, value);
                    }

                    @Override
                    public Function createFunction(final String name) {
                        return SessionService.this.functionFactory.createFunction(name);
                    }
                }));
    }

    protected abstract void executeWithinSessionInternal(String wid, String sid, Map<String, Json> presetAttributes, WithinSessionExecutorInternal executor);


    public final void doWithinIsolatedSession(final Workspace workspace, final RuntimeSession.SessionSettings sessionSettings,
                                              final Map<String, Json> presetAttributes,
                                              final WithinSessionExecutor withinSessionExecutor) {
        final RuntimeSession session = this.startSession(workspace, sessionSettings);
        try {
            this.executeWithinSession(workspace.getId(), session.getId(), presetAttributes, withinSessionExecutor);
        } finally {
            this.stopSession(session);
        }
    }


    public interface VariableBindingRejector {
        void reject(String key, String message);

        boolean hasErrors();

        Exception buildException();
    }
}
