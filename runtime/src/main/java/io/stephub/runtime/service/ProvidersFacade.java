package io.stephub.runtime.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.stephub.expression.AttributesContext;
import io.stephub.json.Json;
import io.stephub.json.JsonObject;
import io.stephub.json.schema.JsonSchema;
import io.stephub.provider.api.Provider;
import io.stephub.provider.api.model.ProviderOptions;
import io.stephub.provider.api.model.StepRequest;
import io.stephub.provider.api.model.StepResponse;
import io.stephub.provider.api.model.spec.StepSpec;
import io.stephub.provider.remote.RemoteProvider;
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

import static io.stephub.provider.api.model.StepResponse.StepStatus.ERRONEOUS;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
@Slf4j
public class ProvidersFacade {

    @Autowired
    private BaseProvider baseProvider;

    @Autowired
    private GherkinPatternMatcher patternMatcher;

    @Autowired
    private StepRequestEvaluator stepRequestEvaluator;

    @Autowired
    private ObjectMapper objectMapper;

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
                                this.getProvider(providerSpec).getInfo().getSteps()
                        )
        );
        return steps;
    }

    public StepResponse<Json> execute(final Workspace workspace, final StepExecution execution, final ProviderSessionStore providerSessionStore, final AttributesContext attributesContext) {
        try {
            final Map<String, List<StepSpec<JsonSchema>>> stepSpecs = this.getStepsCollection(workspace);
            for (final String providerName : stepSpecs.keySet()) {
                for (final StepSpec<JsonSchema> s : stepSpecs.get(providerName)) {
                    final GherkinPatternMatcher.StepMatch stepMatch = this.patternMatcher.matches(s, execution.getInstruction());
                    if (stepMatch != null) {
                        final StepRequest.StepRequestBuilder<Json> requestBuilder = StepRequest.<Json>builder().id(s.getId());
                        this.stepRequestEvaluator.populateRequest(stepMatch, requestBuilder, attributesContext);
                        final ProviderSpec providerSpec = this.getProviderSpecs(workspace).stream().filter(ps -> ps.getName().equals(providerName)).findFirst().get();
                        return this.execute(
                                this.getProvider(providerSpec),
                                providerSessionStore,
                                providerSpec,
                                requestBuilder);
                    }
                }
            }
        } catch (final Exception e) {
            return StepResponse.<Json>builder().status(ERRONEOUS).
                    errorMessage(e.getMessage()).
                    build();
        }
        return StepResponse.<Json>builder().status(ERRONEOUS).
                errorMessage("No step found matching the instruction '" + execution.getInstruction() + "'").
                build();
    }

    public Provider<JsonObject, JsonSchema, Json> getProvider(final ProviderSpec providerSpec) {
        if (BaseProvider.PROVIDER_NAME.equals(providerSpec.getName())) {
            return this.baseProvider;
        } else {
            return RemoteProvider.builder().baseUrl(providerSpec.getProviderUrl()).
                    objectMapper(this.objectMapper).
                    alias(providerSpec.getName() +
                            (isNotBlank(providerSpec.getVersion()) ? ":" + providerSpec.getVersion() : "")
                    ).build();
        }
    }

    private StepResponse<Json> execute(final Provider<JsonObject, JsonSchema, Json> provider,
                                       final ProviderSessionStore providerSessionStore,
                                       final ProviderOptions<JsonObject> providerOptions,
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
