package io.stephub.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.stephub.expression.EvaluationContext;
import io.stephub.json.Json;
import io.stephub.json.JsonObject;
import io.stephub.json.schema.JsonSchema;
import io.stephub.provider.api.Provider;
import io.stephub.provider.api.ProviderException;
import io.stephub.provider.api.model.ProviderOptions;
import io.stephub.provider.api.model.StepRequest;
import io.stephub.provider.api.model.StepResponse;
import io.stephub.provider.api.model.spec.StepSpec;
import io.stephub.provider.remote.RemoteProvider;
import io.stephub.providers.base.BaseProvider;
import io.stephub.server.api.SessionExecutionContext;
import io.stephub.server.api.StepExecution;
import io.stephub.server.api.model.ProviderSpec;
import io.stephub.server.api.model.Workspace;
import io.stephub.server.service.GherkinPatternMatcher.StepMatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Validator;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.stephub.provider.api.model.StepResponse.StepStatus.ERRONEOUS;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
@Slf4j
public class ProvidersFacade implements StepExecutionSource {

    @Autowired
    private BaseProvider baseProvider;

    @Autowired
    private GherkinPatternMatcher patternMatcher;

    @Autowired
    private StepEvaluationDelegate stepEvaluationDelegate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Validator validator;

    @Override
    public StepExecution resolveStepExecution(final String stepInstruction, final Workspace workspace) {
        final Triple<ProviderSpec, StepSpec<JsonSchema>, StepMatch> match = this.getMatchingStep(workspace, stepInstruction);
        if (match == null) {
            return null;
        }
        final ProviderSpec providerSpec = match.getLeft();
        final StepSpec<JsonSchema> stepSpec = match.getMiddle();
        final StepMatch stepMatch = match.getRight();
        return new StepExecution() {
            @Override
            public StepResponse<Json> execute(final SessionExecutionContext sessionExecutionContext, final EvaluationContext evaluationContext) {
                final long start = System.currentTimeMillis();
                try {
                    final StepEvaluationDelegate.StepEvaluation stepEvaluation = ProvidersFacade.this.stepEvaluationDelegate.getStepEvaluation(stepMatch, evaluationContext);
                    final StepResponse<Json> response = ProvidersFacade.this.execute(
                            ProvidersFacade.this.getProvider(providerSpec),
                            sessionExecutionContext,
                            providerSpec,
                            stepEvaluation.getRequestBuilder().id(stepSpec.getId()).build());
                    stepEvaluation.postEvaluateResponse(response);
                    return response;
                } catch (final Exception e) {
                    return StepResponse.<Json>builder().status(ERRONEOUS).
                            errorMessage(e.getMessage()).
                            duration(Duration.ofMillis(System.currentTimeMillis() - start)).
                            build();
                }
            }

            @Override
            public StepSpec<JsonSchema> getStepSpec() {
                return stepSpec;
            }
        };
    }


    private List<ProviderSpec> getProviderSpecs(final Workspace workspace) {
        final List<ProviderSpec> providerSpecs = new ArrayList<>(workspace.getProviders());
        providerSpecs.add(ProviderSpec.builder().name(BaseProvider.PROVIDER_NAME).version(">0").build());
        return providerSpecs;
    }

    private Triple<ProviderSpec, StepSpec<JsonSchema>, StepMatch> getMatchingStep(final Workspace workspace, final String instruction) {
        for (final ProviderSpec providerSpec : this.getProviderSpecs(workspace)) {
            try {
                for (final StepSpec<JsonSchema> stepSpec : this.getProvider(providerSpec).getInfo().getSteps()) {
                    final StepMatch stepMatch = this.patternMatcher.matches(workspace.getGherkinPreferences(), stepSpec, instruction);
                    if (stepMatch != null) {
                        return Triple.of(providerSpec, stepSpec, stepMatch);
                    }
                }
            } catch (final ProviderException e) {
                log.debug("Failed to check provider " + providerSpec + " for matching step", e);
            }
        }
        return null;
    }

    public Map<String, List<StepSpec<JsonSchema>>> getStepsCollection(final Workspace workspace) {
        final Map<String, List<StepSpec<JsonSchema>>> steps = new HashMap<>();
        this.getProviderSpecs(workspace).forEach(
                providerSpec ->
                        steps.put(providerSpec.getName(),
                                this.getProvider(providerSpec).getInfo().getSteps()
                        )
        );
        return steps;
    }

    public Provider<JsonObject, JsonSchema, Json> getProvider(final ProviderSpec providerSpec) {
        if (BaseProvider.PROVIDER_NAME.equals(providerSpec.getName())) {
            return this.baseProvider;
        } else {
            return RemoteProvider.builder().baseUrl(providerSpec.getProviderUrl()).
                    objectMapper(this.objectMapper).
                    alias(providerSpec.getName() +
                            (isNotBlank(providerSpec.getVersion()) ? ":" + providerSpec.getVersion() : "")
                    ).
                    validator(this.validator).
                    build();
        }
    }

    private StepResponse<Json> execute(final Provider<JsonObject, JsonSchema, Json> provider,
                                       final SessionExecutionContext sessionExecutionContext,
                                       final ProviderOptions<JsonObject> providerOptions,
                                       final StepRequest<Json> request) {
        final String providerName = provider.getInfo().getName();
        String pSid = sessionExecutionContext.getProviderSession(providerName);
        if (pSid == null) {
            pSid = provider.createSession(providerOptions);
            log.debug("Retrieved new session id={} from provider={}", pSid, providerName);
            sessionExecutionContext.setProviderSession(providerName, pSid);
        }
        return provider.execute(pSid, request);
    }
}
