package io.stephub.provider;

public interface Provider {
    String createSession(ProviderOptions options) throws ProviderException;

    StepResponse execute(String sessionId, StepRequest request) throws ProviderException;

    void destroySession(String sessionId) throws ProviderException;

    ProviderInfo getInfo() throws ProviderException;

}
