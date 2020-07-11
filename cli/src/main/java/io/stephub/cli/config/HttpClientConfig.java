package io.stephub.cli.config;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class HttpClientConfig {
    @Bean
    public OkHttpClient.Builder httpClientBuilder() {
        return new OkHttpClient.Builder().addInterceptor(new HttpLoggingInterceptor(
                new HttpLoggingInterceptor.Logger() {
                    @Override
                    public void log(final String s) {
                        log.debug(s);
                    }
                }
        ).setLevel(HttpLoggingInterceptor.Level.BASIC));
    }
}
