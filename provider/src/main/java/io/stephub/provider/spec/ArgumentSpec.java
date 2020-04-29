package io.stephub.provider.spec;

import io.stephub.json.schema.JsonSchema;
import lombok.*;

import static io.stephub.json.Json.JsonType.JSON;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@EqualsAndHashCode
@ToString
public class ArgumentSpec {
    private String name;
    @Builder.Default
    private JsonSchema schema = JsonSchema.ofType(JSON);
}
