package io.stephub.runtime.service;

import io.stephub.expression.impl.SimpleEvaluationContext;
import io.stephub.json.Json;
import io.stephub.provider.api.model.StepResponse;
import io.stephub.runtime.model.Context;
import io.stephub.runtime.model.RuntimeSession;
import io.stephub.runtime.model.StepInstruction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static io.stephub.provider.api.model.StepResponse.StepStatus.ERRONEOUS;
import static io.stephub.runtime.model.RuntimeSession.SessionStatus.INACTIVE;

@Slf4j
public abstract class SessionService {
    @Autowired
    private StepExecutionResolver stepExecutionResolver;

    public abstract List<RuntimeSession> getSessions(Context ctx, String wid);

    public abstract RuntimeSession startSession(Context ctx, String wid);

    public abstract void stopSession(Context ctx, String wid, String sid);

    public abstract RuntimeSession getSession(Context ctx, String wid, String sid);

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
                            SimpleEvaluationContext.builder().build());
                }
        );
    }

    public static final StepResponse<Json> buildResponseForMissingStep(final String instruction) {
        return StepResponse.<Json>builder().status(ERRONEOUS).
                errorMessage("No step found matching the instruction '" + instruction + "'").
                build();
    }
}
