package io.stephub.provider;

import io.stephub.json.schema.JsonSchema;
import io.stephub.provider.spec.StepSpec;
import lombok.*;

import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class ProviderInfo {
    private String name;
    private String version;
    private JsonSchema optionsSchema;
    private List<StepSpec> steps;
}
