package io.stephub.server.service.executor;

import io.stephub.expression.EvaluationContext;
import io.stephub.json.Json;
import io.stephub.provider.api.model.StepResponse;
import io.stephub.server.api.SessionExecutionContext;
import io.stephub.server.api.StepExecution;
import io.stephub.server.api.model.Execution;
import io.stephub.server.api.model.Workspace;
import io.stephub.server.service.ExecutionPersistence;
import io.stephub.server.service.ExecutionPersistence.StepExecutionResult;
import io.stephub.server.service.StepExecutionResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import static io.stephub.provider.api.model.StepResponse.StepStatus.ERRONEOUS;
import static io.stephub.provider.api.model.StepResponse.StepStatus.PASSED;
import static io.stephub.server.api.StepExecution.buildResponseForMissingStep;

@Slf4j
public abstract class Executor<I extends Execution.ExecutionItem> {
    @Autowired
    private StepExecutionResolver stepExecutionResolver;

    public abstract void execute(Workspace workspace, I item, SessionExecutionContext sessionExecutionContext, EvaluationContext evaluationContext, ExecutionPersistence.StepExecutionFacade stepExecutionFacade);

    protected boolean executeStepItem(final Execution.StepExecutionItem stepItem, final Workspace workspace, final SessionExecutionContext sessionExecutionContext, final EvaluationContext evaluationContext, final ExecutionPersistence.StepExecutionFacade stepExecutionFacade) {
        final StepResponse<Json> response = stepExecutionFacade.doStep(stepItem, () -> {
            try {
                final String stepInstruction = stepItem.getStep();
                final StepExecution stepExecution = this.stepExecutionResolver.resolveStepExecution(stepInstruction, workspace);
                if (stepExecution == null) {
                    return StepExecutionResult.builder().response(buildResponseForMissingStep(stepInstruction)).build();
                } else {
                    return StepExecutionResult.builder().
                            response(stepExecution.execute(sessionExecutionContext, evaluationContext)).
                            stepSpec(stepExecution.getStepSpec()).build();
                }
            } catch (final Exception e) {
                log.error("Unexpected step execution error", e);
                return StepExecutionResult.builder().response(
                        StepResponse.<Json>builder().status(ERRONEOUS).
                                errorMessage("Unexpected exception: " + e.getMessage()).
                                build()).build();
            }
        });
        return response.getStatus() == PASSED;
    }
}
