package io.stephub.server.api;

public interface SessionExecutionContext {
    void setProviderSession(String providerName, String sid);

    String getProviderSession(String providerName);
}
