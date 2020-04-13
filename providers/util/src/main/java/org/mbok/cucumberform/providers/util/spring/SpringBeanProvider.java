package org.mbok.cucumberform.providers.util.spring;

import org.mbok.cucumberform.provider.StepRequest;
import org.mbok.cucumberform.provider.StepResponse;
import org.mbok.cucumberform.provider.spec.StepSpec;
import org.mbok.cucumberform.providers.util.LocalProviderAdapter;

import java.util.*;

public abstract class SpringBeanProvider<S extends LocalProviderAdapter.SessionState> extends LocalProviderAdapter<S> {
    Map<String, StepInvoker> stepInvokers = new HashMap<>();
    List<StepSpec> stepSpecs = new ArrayList<>();

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

    @Override
    public List<StepSpec> getSteps() {
        return stepSpecs;
    }
}
