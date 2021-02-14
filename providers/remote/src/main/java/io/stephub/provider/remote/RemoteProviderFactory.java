package io.stephub.provider.remote;

import io.stephub.server.api.model.ProviderSpec;


public interface RemoteProviderFactory {

    RemoteProvider createProvider(ProviderSpec remoteSpec);
}
