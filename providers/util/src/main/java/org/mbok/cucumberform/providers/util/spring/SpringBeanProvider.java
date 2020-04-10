package org.mbok.cucumberform.providers.util.spring;

import org.mbok.cucumberform.provider.StepRequest;
import org.mbok.cucumberform.provider.StepResponse;
import org.mbok.cucumberform.providers.util.LocalProviderAdapter;

import java.util.HashMap;
import java.util.Map;

public abstract class SpringBeanProvider<S extends LocalProviderAdapter.SessionState> extends LocalProviderAdapter<S> {
    Map<String, StepInvoker> stepInvokers = new HashMap<>();

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


}
