package io.stephub.runtime.service.executor;

import io.stephub.expression.EvaluationContext;
import io.stephub.json.Json;
import io.stephub.provider.api.model.StepResponse;
import io.stephub.runtime.model.Execution;
import io.stephub.runtime.model.Workspace;
import io.stephub.runtime.service.ExecutionPersistence;
import io.stephub.runtime.service.SessionExecutionContext;
import io.stephub.runtime.service.StepExecution;
import io.stephub.runtime.service.StepExecutionResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import static io.stephub.provider.api.model.StepResponse.StepStatus.ERRONEOUS;
import static io.stephub.provider.api.model.StepResponse.StepStatus.PASSED;

@Slf4j
public abstract class Executor<I extends Execution.ExecutionItem> {
    @Autowired
    private StepExecutionResolver stepExecutionResolver;

    public abstract void execute(Workspace workspace, I item, SessionExecutionContext sessionExecutionContext, EvaluationContext evaluationContext, ExecutionPersistence.StepExecutionFacade stepExecutionFacade);

    public static StepResponse<Json> buildResponseForMissingStep(final String instruction) {
        return StepResponse.<Json>builder().status(ERRONEOUS).
                errorMessage("No step found matching the instruction '" + instruction + "'").
                build();
    }

    protected boolean executeStepItem(final Execution.StepExecutionItem stepItem, final Workspace workspace, final SessionExecutionContext sessionExecutionContext, final EvaluationContext evaluationContext, final ExecutionPersistence.StepExecutionFacade stepExecutionFacade) {
        final StepResponse<Json> response = stepExecutionFacade.doStep(stepItem, () -> {
            try {
                final String stepInstruction = stepItem.getInstruction();
                final StepExecution stepExecution = this.stepExecutionResolver.resolveStepExecution(stepInstruction, workspace);
                if (stepExecution == null) {
                    return buildResponseForMissingStep(stepInstruction);
                } else {
                    return stepExecution.execute(sessionExecutionContext, evaluationContext);
                }
            } catch (final Exception e) {
                log.error("Unexpected step execution error", e);
                return StepResponse.<Json>builder().status(ERRONEOUS).
                        errorMessage("Unexpected exception: " + e.getMessage()).
                        build();
            }
        });
        return response.getStatus() == PASSED;
    }
}
