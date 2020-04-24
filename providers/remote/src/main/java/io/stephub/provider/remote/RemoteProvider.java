package io.stephub.provider.remote;

import io.stephub.json.schema.JsonSchema;
import io.stephub.provider.Provider;
import io.stephub.provider.StepRequest;
import io.stephub.provider.StepResponse;
import io.stephub.provider.spec.StepSpec;

import java.util.List;

public class RemoteProvider implements Provider {

    @Override
    public String createSession(final ProviderOptions options) {
        return null;
    }

    @Override
    public StepResponse execute(final String sessionId, final StepRequest request) {

        return null;
    }

    @Override
    public void destroySession(final String sessionId) {

    }

    @Override
    public List<StepSpec> getSteps() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public JsonSchema getOptionsSchema() {
        return new JsonSchema();
    }

}
