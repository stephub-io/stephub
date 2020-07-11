package io.stephub.server.service.executor;

import io.stephub.expression.EvaluationContext;
import io.stephub.server.api.SessionExecutionContext;
import io.stephub.server.api.model.Execution;
import io.stephub.server.api.model.Workspace;
import io.stephub.server.service.ExecutionPersistence;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StepExecutor extends Executor<Execution.StepExecutionItem> {

    @Override
    public void execute(final Workspace workspace, final Execution.StepExecutionItem item, final SessionExecutionContext sessionExecutionContext, final EvaluationContext evaluationContext, final ExecutionPersistence.StepExecutionFacade stepExecutionFacade) {
        this.executeStepItem(item, workspace, sessionExecutionContext, evaluationContext, stepExecutionFacade);
    }
}
