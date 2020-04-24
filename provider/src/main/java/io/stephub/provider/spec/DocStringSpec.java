package io.stephub.provider.spec;

import io.stephub.json.schema.JsonSchema;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@ToString
@EqualsAndHashCode
public class DocStringSpec {
    private JsonSchema schema;
}
