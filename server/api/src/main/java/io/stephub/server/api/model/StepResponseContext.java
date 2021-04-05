package io.stephub.server.api.model;

import io.stephub.json.Json;
import io.stephub.provider.api.model.StepResponse;

import java.util.Optional;

public interface StepResponseContext {
    void completeStep(StepResponse<Json> response);

    NestedResponseContext nested();

    boolean continuable();

    interface NestedResponseContext {
        NestedResponseSequenceContext group(Optional<String> name);

        boolean continuable();
    }

    interface NestedResponseSequenceContext {
        StepResponseContext startStep(String step);

        boolean continuable();
    }
}
