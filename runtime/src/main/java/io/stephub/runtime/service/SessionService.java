package io.stephub.runtime.service;

import io.stephub.expression.EvaluationContext;
import io.stephub.expression.FunctionFactory;
import io.stephub.json.Json;
import io.stephub.json.JsonNull;
import io.stephub.json.JsonObject;
import io.stephub.json.schema.JsonInvalidSchemaException;
import io.stephub.provider.api.model.StepResponse;
import io.stephub.runtime.model.Context;
import io.stephub.runtime.model.RuntimeSession;
import io.stephub.runtime.model.StepInstruction;
import io.stephub.runtime.service.exception.ExecutionException;
import io.stephub.runtime.service.exception.ExecutionPrerequisiteException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.stephub.provider.api.model.StepResponse.StepStatus.ERRONEOUS;
import static io.stephub.runtime.model.RuntimeSession.SessionStatus.INACTIVE;

@Slf4j
public abstract class SessionService {
    @Autowired
    private StepExecutionResolver stepExecutionResolver;

    @Autowired
    private FunctionFactory functionFactory;

    public abstract List<RuntimeSession> getSessions(Context ctx, String wid);

    public abstract RuntimeSession startSession(Context ctx, String wid, RuntimeSession.SessionStart sessionStart);

    public abstract void stopSession(Context ctx, String wid, String sid);

    public abstract RuntimeSession getSession(Context ctx, String wid, String sid);

    protected void setUpAttributes(final RuntimeSession session, final RuntimeSession.SessionStart sessionStart) {
        final Map<String, Json> attributes = new HashMap<>();
        final JsonObject vars = new JsonObject();
        session.getWorkspace().getVariables().forEach((key, var) -> {
            Json value = sessionStart.getVariables().get(key);
            if (value == null) {
                if (var.getValue() != JsonNull.INSTANCE) {
                    value = var.getValue();
                } else {
                    value = var.getDefaultValue();
                }
            }
            try {
                var.getSchema().accept(value);
            } catch (final JsonInvalidSchemaException e) {
                throw new ExecutionPrerequisiteException("Invalid value for variable '" + key + "': " + e.getMessage());
            }
            vars.getFields().put(key, value);
        });
        session.getAttributes().put("var", vars);
    }

    public interface WinthinSessionExecutor<T> {
        T execute(RuntimeSession session, SessionExecutionContext sessionExecutionContext);
    }

    public abstract <T> T executeWithinSession(Context ctx, String wid, String sid, WinthinSessionExecutor<T> executor);

    public final StepResponse<Json> execute(final Context ctx, final String wid, final String sid, final StepInstruction stepInstruction) {
        return this.executeWithinSession(ctx, wid, sid, (session, sessionExecutionContext) -> {
                    if (session.getStatus() == INACTIVE) {
                        throw new ExecutionException("Session isn't active with id=" + sid);
                    }
                    log.debug("Execute within session={} the step={}", session, stepInstruction);
                    final StepExecution stepExecution = this.stepExecutionResolver.resolveStepExecution(stepInstruction, session.getWorkspace());
                    if (stepExecution == null) {
                        return buildResponseForMissingStep(stepInstruction.getInstruction());
                    }
                    return stepExecution.execute(sessionExecutionContext,
                            new EvaluationContext() {
                                @Override
                                public Json get(final String key) {
                                    return session.getAttributes().get(key);
                                }

                                @Override
                                public void put(final String key, final Json value) {
                                    session.getAttributes().put(key, value);
                                }

                                @Override
                                public Function createFunction(final String name) {
                                    return SessionService.this.functionFactory.createFunction(name);
                                }
                            });
                }
        );
    }

    public static final StepResponse<Json> buildResponseForMissingStep(final String instruction) {
        return StepResponse.<Json>builder().status(ERRONEOUS).
                errorMessage("No step found matching the instruction '" + instruction + "'").
                build();
    }
}
