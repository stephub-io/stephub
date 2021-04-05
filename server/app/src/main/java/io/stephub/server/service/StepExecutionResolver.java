package io.stephub.server.service;

import io.stephub.expression.EvaluationContext;
import io.stephub.expression.ExpressionEvaluator;
import io.stephub.json.Json;
import io.stephub.json.JsonArray;
import io.stephub.json.JsonObject;
import io.stephub.json.schema.JsonSchema;
import io.stephub.provider.api.model.StepRequest;
import io.stephub.provider.api.model.StepResponse;
import io.stephub.provider.api.model.spec.StepSpec;
import io.stephub.server.api.SessionExecutionContext;
import io.stephub.server.api.StepExecution;
import io.stephub.server.api.model.GherkinPreferences;
import io.stephub.server.api.model.StepResponseContext;
import io.stephub.server.api.model.Workspace;
import io.stephub.server.api.model.customsteps.CustomStepContainer;
import io.stephub.server.api.model.customsteps.StepDefinition;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static io.stephub.provider.api.model.StepResponse.StepStatus.ERRONEOUS;
import static io.stephub.server.api.StepExecution.buildResponseForMissingStep;

@Service
@Slf4j
public class StepExecutionResolver implements StepExecutionSource {
    public static final String RECURSIVE_STEP_CALL_SEQUENCE_DETECTED = "Recursive step call sequence detected";
    @Autowired
    private ProvidersFacade providersFacade;

    @Autowired
    private GherkinPatternMatcher patternMatcher;

    @Autowired
    private StepEvaluationDelegate stepEvaluationDelegate;

    @Autowired
    private ExpressionEvaluator expressionEvaluator;

    public StepExecution resolveAlways(final String stepInstruction, final Workspace workspace) {
        try {
            final StepExecution stepExecution = this.resolveStepExecution(stepInstruction, workspace);
            if (stepExecution == null) {
                return new StepExecution() {
                    @Override
                    public void execute(final SessionExecutionContext sessionExecutionContext, final EvaluationContext evaluationContext, final StepResponseContext responseContext) {
                        responseContext.completeStep(buildResponseForMissingStep(stepInstruction));
                    }

                    @Override
                    public StepSpec<JsonSchema> getStepSpec() {
                        return null;
                    }
                };
            } else {
                return new StepExecution() {
                    @Override
                    public void execute(final SessionExecutionContext sessionExecutionContext, final EvaluationContext evaluationContext, final StepResponseContext responseContext) {
                        try {
                            stepExecution.execute(sessionExecutionContext, evaluationContext, responseContext);
                        } catch (final Exception e) {
                            log.error("Unexpected step execution error", e);
                            responseContext.completeStep(StepResponse.<Json>builder().status(ERRONEOUS).
                                    errorMessage("Unexpected exception: " + e.getMessage()).
                                    build());
                        }
                    }

                    @Override
                    public StepSpec<JsonSchema> getStepSpec() {
                        return stepExecution.getStepSpec();
                    }
                };
            }
        } catch (final Exception e) {
            return new StepExecution() {
                @Override
                public void execute(final SessionExecutionContext sessionExecutionContext, final EvaluationContext evaluationContext, final StepResponseContext responseContext) {
                    log.error("Unexpected step execution error", e);
                    responseContext.completeStep(StepResponse.<Json>builder().status(ERRONEOUS).
                            errorMessage("Unexpected exception: " + e.getMessage()).
                            build());
                }

                @Override
                public StepSpec<JsonSchema> getStepSpec() {
                    return null;
                }
            };
        }
    }

    @Override
    public StepExecution resolveStepExecution(final String stepInstruction, final Workspace workspace) {
        return new StepExecutionResolver.HierarchicalResolver(
                workspace.getGherkinPreferences(),
                instruction ->
                        this.providersFacade.resolveStepExecution(
                                instruction,
                                workspace
                        ),
                workspace,
                new HashSet<>()).
                resolveStepExecution(stepInstruction);
    }

    @AllArgsConstructor
    private class HierarchicalResolver implements StepDefinition.StepExecutionResolverWrapper {
        private final GherkinPreferences gherkinPreferences;
        private final StepDefinition.StepExecutionResolverWrapper parentResolver;
        private final CustomStepContainer stepContainer;
        private final HashSet<StepDefinition> stepDefinitionStack;

        @Override
        public StepExecution resolveStepExecution(final String instruction) {
            for (final StepDefinition stepDefinition : this.stepContainer.getStepDefinitions()) {
                final GherkinPatternMatcher.StepMatch match = StepExecutionResolver.this.patternMatcher.matches(this.gherkinPreferences, stepDefinition.getSpec(), instruction);
                if (match != null) {
                    log.trace("Resolved for instruction={} a custom step={}", instruction, stepDefinition);
                    return new StepExecution() {
                        @Override
                        public void execute(final SessionExecutionContext sessionExecutionContext, final EvaluationContext evaluationContext, final StepResponseContext responseContext) {
                            // Check cycle
                            if (HierarchicalResolver.this.stepDefinitionStack.contains(stepDefinition)) {
                                responseContext.completeStep(
                                        StepResponse.<Json>builder().status(ERRONEOUS).
                                                errorMessage(RECURSIVE_STEP_CALL_SEQUENCE_DETECTED).build());
                                return;
                            }
                            // Do step
                            HierarchicalResolver.this.stepDefinitionStack.add(stepDefinition);
                            try {
                                final StepEvaluationDelegate.StepEvaluation stepEvaluation = StepExecutionResolver.this.stepEvaluationDelegate.getStepEvaluation(match, evaluationContext);
                                final StepRequest<Json> stepRequest = stepEvaluation.getRequestBuilder().build();
                                stepDefinition.execute(stepRequest, sessionExecutionContext,
                                        new StepScopedEvaluationContext(evaluationContext, stepRequest),
                                        new HierarchicalResolver(
                                                HierarchicalResolver.this.gherkinPreferences,
                                                HierarchicalResolver.this,
                                                stepDefinition,
                                                HierarchicalResolver.this.stepDefinitionStack),
                                        StepExecutionResolver.this.expressionEvaluator,
                                        new StepResponseContext() {
                                            @Override
                                            public void completeStep(final StepResponse<Json> response) {
                                                stepEvaluation.postEvaluateResponse(response);
                                                responseContext.completeStep(response);
                                            }

                                            @Override
                                            public NestedResponseContext nested() {
                                                return responseContext.nested();
                                            }

                                            @Override
                                            public boolean continuable() {
                                                return responseContext.continuable();
                                            }
                                        }
                                );
                            } finally {
                                HierarchicalResolver.this.stepDefinitionStack.remove(stepDefinition);
                            }
                        }

                        @Override
                        public StepSpec<JsonSchema> getStepSpec() {
                            return stepDefinition.getSpec();
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
            if (key.equals("arg")) {
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
