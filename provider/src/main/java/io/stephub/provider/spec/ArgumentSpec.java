package io.stephub.provider.spec;

import io.stephub.json.schema.JsonSchema;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@EqualsAndHashCode
@ToString
public class ArgumentSpec {
    private String name;
    private JsonSchema schema;
}
