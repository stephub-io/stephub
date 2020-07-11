package io.stephub.server.service.executor;

import io.stephub.expression.EvaluationContext;
import io.stephub.server.api.SessionExecutionContext;
import io.stephub.server.api.model.Execution;
import io.stephub.server.api.model.Workspace;
import io.stephub.server.service.ExecutionPersistence;
import io.stephub.server.service.exception.ExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExecutorDelegate extends Executor<Execution.ExecutionItem> {
    @Autowired
    private StepExecutor stepExecutor;

    @Autowired
    private ScenarioExecutor scenarioExecutor;

    @Override
    public void execute(final Workspace workspace, final Execution.ExecutionItem item, final SessionExecutionContext sessionExecutionContext, final EvaluationContext evaluationContext, final ExecutionPersistence.StepExecutionFacade stepExecutionFacade) {
        if (item instanceof Execution.StepExecutionItem) {
            this.stepExecutor.execute(workspace, (Execution.StepExecutionItem) item, sessionExecutionContext, evaluationContext, stepExecutionFacade);
        } else if (item instanceof Execution.ScenarioExecutionItem) {
            this.scenarioExecutor.execute(workspace, (Execution.ScenarioExecutionItem) item, sessionExecutionContext, evaluationContext, stepExecutionFacade);
        } else {
            throw new ExecutionException("Unknown execution item: " + item);
        }
    }
}
