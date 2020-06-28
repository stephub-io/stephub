package io.stephub.runtime.service.executor;

import io.stephub.expression.EvaluationContext;
import io.stephub.runtime.model.Execution;
import io.stephub.runtime.model.Workspace;
import io.stephub.runtime.service.ExecutionPersistence;
import io.stephub.runtime.service.SessionExecutionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ScenarioExecutor extends Executor<Execution.ScenarioExecutionItem> {

    @Override
    public void execute(final Workspace workspace, final Execution.ScenarioExecutionItem item, final SessionExecutionContext sessionExecutionContext, final EvaluationContext evaluationContext, final ExecutionPersistence.StepExecutionFacade stepExecutionFacade) {
        if (!this.executeFixtures(item, item.getBeforeFixtures(), workspace, sessionExecutionContext, evaluationContext, stepExecutionFacade)) {
            log.debug("Cancel further execution of scenario={} due to faulty before fixtures in workspace={}", item, workspace);
            return;
        }
        for (final Execution.StepExecutionItem step : item.getSteps()) {
            if (!this.executeStepItem(step, workspace, sessionExecutionContext, evaluationContext, stepExecutionFacade)) {
                log.debug("Cancel further execution of scenario={} due to faulty step={} in workspace={}", item, step, workspace);
                return;
            }
        }
        if (!this.executeFixtures(item, item.getAfterFixtures(), workspace, sessionExecutionContext, evaluationContext, stepExecutionFacade)) {
            log.debug("Cancel further execution of scenario={} due to faulty after fixtures in workspace={}", item, workspace);
            return;
        }
    }


    private boolean executeFixtures(final Execution.ScenarioExecutionItem scenario, final List<Execution.FixtureExecutionItem> fixtures, final Workspace workspace, final SessionExecutionContext sessionExecutionContext, final EvaluationContext evaluationContext, final ExecutionPersistence.StepExecutionFacade stepExecutionFacade) {
        for (final Execution.FixtureExecutionItem fixture : fixtures) {
            for (final Execution.StepExecutionItem step : fixture.getSteps()) {
                if (!this.executeStepItem(step, workspace, sessionExecutionContext, evaluationContext, stepExecutionFacade)) {
                    log.debug("Cancel further execution of scenario={} due to faulty step={} in fixture={} in workspace={}", scenario, step, fixture, workspace);
                    return false;
                }
            }
        }
        return true;
    }

}
