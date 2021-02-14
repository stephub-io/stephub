package io.stephub.server.controller;

import io.stephub.json.schema.JsonSchema;
import io.stephub.provider.api.model.ProviderInfo;
import io.stephub.providers.base.BaseProvider;
import io.stephub.server.model.Context;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
public class ProviderController {

    @Autowired
    private BaseProvider baseProvider;

    @GetMapping("/providers/registered")
    @ResponseBody
    public List<RegisteredProvider> getKnowProviders(@ModelAttribute final Context ctx) {
        final List<RegisteredProvider> providers = new ArrayList<>();
        providers.add(RegisteredProvider.from(this.baseProvider.getInfo()));
        return providers;
    }

    @Data
    @Builder
    public static class RegisteredProvider {
        private String name;
        private String version;
        private JsonSchema optionsSchema;

        public static RegisteredProvider from(final ProviderInfo<JsonSchema> providerInfo) {
            return RegisteredProvider.builder().name(providerInfo.getName())
                    .version(providerInfo.getVersion())
                    .optionsSchema(providerInfo.getOptionsSchema())
                    .build();
        }
    }
}
