package org.mbok.cucumberform.runtime.service;

import org.mbok.cucumberform.provider.StepResponse;
import org.mbok.cucumberform.provider.spec.StepSpec;
import org.mbok.cucumberform.providers.base.BaseProvider;
import org.mbok.cucumberform.runtime.model.StepExecution;
import org.mbok.cucumberform.runtime.model.Workspace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProvidersFacade {
    @Autowired
    private BaseProvider baseProvider;

    public interface ProviderSessionStore {
        String getProviderSession(String providerName);
        void setProviderSession(String providerName, String providerSession);
    }

    public Map<String, List<StepSpec>> getStepsCollection(final Workspace workspace) {
        final Map<String, List<StepSpec>> steps = new HashMap<>();
        steps.put(this.baseProvider.getName(), this.baseProvider.getSteps());
        return steps;
    }

    public StepResponse execute(Workspace workspace, StepExecution execution, ProviderSessionStore providerSessionStore) {
        return null;
    }
}
