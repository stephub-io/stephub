package io.stephub.providers.util.spring;

import io.stephub.json.schema.JsonSchema;
import io.stephub.provider.ProviderException;
import io.stephub.provider.ProviderInfo;
import io.stephub.provider.StepRequest;
import io.stephub.provider.StepResponse;
import io.stephub.provider.spec.StepSpec;
import io.stephub.providers.util.LocalProviderAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SpringBeanProvider<S extends LocalProviderAdapter.SessionState> extends LocalProviderAdapter<S> {
    Map<String, StepInvoker> stepInvokers = new HashMap<>();
    List<StepSpec> stepSpecs = new ArrayList<>();

    @Autowired(required = false)
    private BuildProperties buildProperties;

    public interface StepInvoker {
        StepResponse invoke(String sessionId, SessionState state, StepRequest request);
    }

    @Override
    protected final StepResponse executeWithinState(final String sessionId, final S state, final StepRequest request) {
        final StepInvoker stepMethod = this.stepInvokers.get(request.getId());
        if (stepMethod != null) {
            return stepMethod.invoke(sessionId, state, request);
        } else {
            throw new ProviderException("No implementation in " + this.getClass().getName() + " found for step with id=" + request.getId());
        }
    }

    protected abstract String getName();

    protected String getVersion() throws ProviderException {
        return this.buildProperties != null ? this.buildProperties.getVersion() : "unknown";
    }

    protected abstract JsonSchema getOptionsSchema();

    @Override
    public final ProviderInfo getInfo() throws ProviderException {
        return ProviderInfo.builder().name(this.getName()).
                version(this.getVersion()).steps(this.stepSpecs).
                optionsSchema(this.getOptionsSchema()).build();
    }

}
