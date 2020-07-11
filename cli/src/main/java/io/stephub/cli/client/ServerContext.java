package io.stephub.cli.client;

import lombok.Builder;
import lombok.Getter;
import okhttp3.HttpUrl;

import java.net.URL;

@Getter
@Builder
public class ServerContext {
    private final URL baseUrl;

    public final HttpUrl.Builder getBaseApiUrl() {
        return HttpUrl.get(this.baseUrl).newBuilder().addPathSegment("api").addPathSegment("v1");
    }

    @Override
    public String toString() {
        return "server { " +
                "baseUrl=" + this.baseUrl +
                " }";
    }
}
