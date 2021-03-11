package io.stephub.server.api;

import io.stephub.server.api.model.ProviderSpec;

public interface SessionExecutionContext {
    void setProviderSession(ProviderSpec providerSpec, String sid);

    String getProviderSession(ProviderSpec providerSpec);
}
