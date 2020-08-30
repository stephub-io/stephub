package io.stephub.cli.config;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class HttpClientConfig {
    @Autowired(required = false)
    private BuildProperties buildProperties;

    @Bean
    public OkHttpClient.Builder httpClientBuilder() {
        return new OkHttpClient.Builder().addInterceptor(new HttpLoggingInterceptor(
                log::debug
        ).setLevel(HttpLoggingInterceptor.Level.BASIC))
                .addInterceptor(chain -> {
                    final Request originalRequest = chain.request();
                    final Request requestWithUserAgent = originalRequest.newBuilder()
                            .header("User-Agent", this.getUserAgent())
                            .build();
                    return chain.proceed(requestWithUserAgent);
                });
    }

    private String getUserAgent() {
        final StringBuilder ua = new StringBuilder("stephub.cli.v");
        if (this.buildProperties != null) {
            ua.append(this.buildProperties.getVersion());
        }
        return ua.toString();
    }
}
