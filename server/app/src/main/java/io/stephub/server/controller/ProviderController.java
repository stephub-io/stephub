package io.stephub.server.controller;

import io.stephub.json.schema.JsonSchema;
import io.stephub.provider.api.model.ProviderInfo;
import io.stephub.server.api.model.ProviderSpec;
import io.stephub.server.api.validation.ValidProviderSpec;
import io.stephub.server.model.Context;
import io.stephub.server.service.ProvidersFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
public class ProviderController {

    @Autowired
    private ProvidersFacade providersFacade;

    @GetMapping("/providers/registered")
    @ResponseBody
    public List<ProviderInfo<JsonSchema>> getKnowProviders(@ModelAttribute final Context ctx) {
        return this.providersFacade.getRegisteredProviders();
    }

    @PostMapping("/providers/lookup")
    @ResponseBody
    public ProviderInfo<JsonSchema> getKnowProviders(@ModelAttribute final Context ctx, @RequestBody @Valid @ValidProviderSpec final ProviderSpec spec) {
        return this.providersFacade.getProvider(spec).getInfo();
    }
}
