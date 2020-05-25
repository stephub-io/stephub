package io.stephub.runtime.service;

public interface SessionExecutionContext {
    void setProviderSession(String providerName, String sid);

    String getProviderSession(String providerName);
}
