package io.stephub.runtime.service;

import io.stephub.expression.ExpressionEvaluator;
import io.stephub.json.Json;
import io.stephub.provider.api.model.StepRequest;
import io.stephub.provider.api.model.StepResponse;
import io.stephub.runtime.model.StepInstruction;
import io.stephub.runtime.model.Workspace;
import io.stephub.runtime.model.customsteps.CustomStepContainer;
import io.stephub.runtime.model.customsteps.Step;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;

import static io.stephub.provider.api.model.StepResponse.StepStatus.ERRONEOUS;

@Service
@Slf4j
public class StepExecutionResolver implements StepExecutionSource {
    public static final String RECURSIVE_STEP_CALL_SEQUENCE_DETECTED = "Recursive step call sequence detected";
    @Autowired
    private ProvidersFacade providersFacade;

    @Autowired
    private GherkinPatternMatcher patternMatcher;

    @Autowired
    private StepRequestEvaluator stepRequestEvaluator;

    @Autowired
    private ExpressionEvaluator expressionEvaluator;

    @Override
    public StepExecution resolveStepExecution(final StepInstruction stepInstruction, final Workspace workspace) {
        return new StepExecutionResolver.HierarchicalResolver(
                instruction ->
                        this.providersFacade.resolveStepExecution(
                                StepInstruction.builder().instruction(instruction).build(),
                                workspace
                        ),
                workspace,
                new HashSet<>()).
                resolveStepExecution(stepInstruction.getInstruction());
    }

    @AllArgsConstructor
    private class HierarchicalResolver implements Step.StepExecutionResolverWrapper {
        private final Step.StepExecutionResolverWrapper parentResolver;
        private final CustomStepContainer stepContainer;
        private final HashSet<Step> stepStack;

        @Override
        public StepExecution resolveStepExecution(final String instruction) {
            for (final Step step : this.stepContainer.getSteps()) {
                final GherkinPatternMatcher.StepMatch match = StepExecutionResolver.this.patternMatcher.matches(step.getSpec(), instruction);
                if (match != null) {
                    log.trace("Resolved for instruction={} a custom step={}", instruction, step);
                    return (sessionExecutionContext, evaluationContext) -> {
                        // Check cycle
                        if (this.stepStack.contains(step)) {
                            return StepResponse.<Json>builder().status(ERRONEOUS).
                                    errorMessage(RECURSIVE_STEP_CALL_SEQUENCE_DETECTED).build();
                        }
                        // Do step
                        this.stepStack.add(step);
                        try {
                            final StepRequest.StepRequestBuilder<Json, ?, ?> requestBuilder = StepRequest.builder();
                            StepExecutionResolver.this.stepRequestEvaluator.populateRequest(match, requestBuilder, evaluationContext);
                            return step.execute(requestBuilder.build(), sessionExecutionContext, evaluationContext,
                                    new StepExecutionResolver.HierarchicalResolver(
                                            this,
                                            step,
                                            this.stepStack),
                                    StepExecutionResolver.this.expressionEvaluator
                            );
                        } finally {
                            this.stepStack.remove(step);
                        }
                    };
                }
            }
            return this.parentResolver.resolveStepExecution(instruction);
        }
    }
}
