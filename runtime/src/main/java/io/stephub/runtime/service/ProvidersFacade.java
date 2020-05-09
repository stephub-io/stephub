package io.stephub.runtime.service;

import io.stephub.expression.AttributesContext;
import io.stephub.json.Json;
import io.stephub.json.schema.JsonSchema;
import io.stephub.provider.api.Provider;
import io.stephub.provider.api.model.ProviderOptions;
import io.stephub.provider.api.model.StepRequest;
import io.stephub.provider.api.model.StepResponse;
import io.stephub.provider.api.model.spec.StepSpec;
import io.stephub.providers.base.BaseProvider;
import io.stephub.runtime.model.ProviderSpec;
import io.stephub.runtime.model.StepExecution;
import io.stephub.runtime.model.Workspace;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ProvidersFacade {

    @Autowired
    private BaseProvider baseProvider;

    @Autowired
    private GherkinPatternMatcher patternMatcher;

    @Autowired
    private StepRequestEvaluator stepRequestEvaluator;

    public interface ProviderSessionStore {
        String getProviderSession(String providerName);

        void setProviderSession(String providerName, String providerSession);
    }

    private List<ProviderSpec> getProviderSpecs(final Workspace workspace) {
        final List<ProviderSpec> providerSpecs = new ArrayList<>(workspace.getProviders());
        providerSpecs.add(ProviderSpec.builder().name(BaseProvider.PROVIDER_NAME).version(">0").build());
        return providerSpecs;
    }

    public Map<String, List<StepSpec<JsonSchema>>> getStepsCollection(final Workspace workspace) {
        final Map<String, List<StepSpec<JsonSchema>>> steps = new HashMap<>();
        this.getProviderSpecs(workspace).forEach(
                providerSpec ->
                        steps.put(providerSpec.getName(),
                                this.getProvider(workspace, providerSpec).getInfo().getSteps()
                        )
        );
        return steps;
    }

    public StepResponse<Json> execute(final Workspace workspace, final StepExecution execution, final ProviderSessionStore providerSessionStore, final AttributesContext attributesContext) {
        final Map<String, List<StepSpec<JsonSchema>>> stepSpecs = this.getStepsCollection(workspace);
        for (final String providerName : stepSpecs.keySet()) {
            for (final StepSpec<JsonSchema> s : stepSpecs.get(providerName)) {
                final GherkinPatternMatcher.StepMatch stepMatch = this.patternMatcher.matches(s, execution.getInstruction());
                if (stepMatch != null) {
                    final StepRequest.StepRequestBuilder<Json> requestBuilder = StepRequest.<Json>builder().id(s.getId());
                    this.stepRequestEvaluator.populateRequest(stepMatch, requestBuilder, attributesContext);
                    final ProviderSpec providerSpec = this.getProviderSpecs(workspace).stream().filter(ps -> ps.getName().equals(providerName)).findFirst().get();
                    return this.execute(
                            this.getProvider(workspace, providerSpec),
                            providerSessionStore,
                            providerSpec,
                            requestBuilder);
                }
            }
        }
        throw new ExecutionException("No step found matching the instruction='" + execution.getInstruction() + "'");
    }

    private Provider<Json, JsonSchema, Json> getProvider(final Workspace workspace, final ProviderSpec providerSpec) {
        // TODO
        return this.baseProvider;
    }

    private StepResponse<Json> execute(final Provider<Json, JsonSchema, Json> provider,
                                       final ProviderSessionStore providerSessionStore,
                                       final ProviderOptions<Json> providerOptions,
                                       final StepRequest.StepRequestBuilder<Json> requestBuilder) {
        final String providerName = provider.getInfo().getName();
        String pSid = providerSessionStore.getProviderSession(providerName);
        if (pSid == null) {
            pSid = provider.createSession(providerOptions);
            log.debug("Retrieved new session id={} from provider={}", pSid, providerName);
            providerSessionStore.setProviderSession(providerName, pSid);
        }
        return provider.execute(pSid, requestBuilder.build());
    }
}
