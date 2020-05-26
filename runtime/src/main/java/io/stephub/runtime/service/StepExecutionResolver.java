package io.stephub.runtime.service;

import io.stephub.expression.EvaluationContext;
import io.stephub.expression.ExpressionEvaluator;
import io.stephub.json.Json;
import io.stephub.json.JsonArray;
import io.stephub.json.JsonObject;
import io.stephub.provider.api.model.StepRequest;
import io.stephub.provider.api.model.StepResponse;
import io.stephub.runtime.model.GherkinPreferences;
import io.stephub.runtime.model.StepInstruction;
import io.stephub.runtime.model.Workspace;
import io.stephub.runtime.model.customsteps.CustomStepContainer;
import io.stephub.runtime.model.customsteps.Step;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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
                workspace,
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
        private final GherkinPreferences gherkinPreferences;
        private final Step.StepExecutionResolverWrapper parentResolver;
        private final CustomStepContainer stepContainer;
        private final HashSet<Step> stepStack;

        @Override
        public StepExecution resolveStepExecution(final String instruction) {
            for (final Step step : this.stepContainer.getSteps()) {
                final GherkinPatternMatcher.StepMatch match = StepExecutionResolver.this.patternMatcher.matches(this.gherkinPreferences, step.getSpec(), instruction);
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
                            final StepRequest<Json> stepRequest = requestBuilder.build();
                            return step.execute(stepRequest, sessionExecutionContext,
                                    new StepScopedEvaluationContext(evaluationContext, stepRequest),
                                    new StepExecutionResolver.HierarchicalResolver(
                                            this.gherkinPreferences,
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

    private static class StepScopedEvaluationContext implements EvaluationContext {
        private final EvaluationContext targetContext;
        private final StepRequest<Json> stepRequest;
        private final Map<String, Json> stepAttributes = new HashMap<>();
        private Json dataTable;

        public StepScopedEvaluationContext(final EvaluationContext targetContext, final StepRequest<Json> stepRequest) {
            this.targetContext = targetContext;
            this.stepRequest = stepRequest;
        }


        @Override
        public Json get(final String key) {
            if (key.equals("args")) {
                return new JsonObject(this.stepRequest.getArguments());
            } else if (key.equals("docString")) {
                return this.stepRequest.getDocString();
            } else if (key.equals("dataTable")) {
                return this.getDataTable();
            } else if (this.stepAttributes.containsKey(key)) {
                return this.stepAttributes.get(key);
            }
            return this.targetContext.get(key);
        }

        @Override
        public void put(final String key, final Json value) {
            this.stepAttributes.put(key, value);
        }

        private JsonArray getDataTable() {
            if (this.dataTable == null && this.stepRequest.getDataTable() != null) {
                final JsonArray rows = new JsonArray();
                for (final Map<String, Json> row : this.stepRequest.getDataTable()) {
                    rows.getValues().add(new JsonObject(row));
                }
                this.dataTable = rows;
            }
            return null;
        }

        @Override
        public Function createFunction(final String name) {
            return this.targetContext.createFunction(name);
        }
    }
}
