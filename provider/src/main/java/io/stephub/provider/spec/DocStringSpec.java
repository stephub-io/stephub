package io.stephub.provider.spec;

import io.stephub.json.schema.JsonSchema;
import lombok.*;

import static io.stephub.json.Json.JsonType.STRING;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@ToString
@EqualsAndHashCode
public class DocStringSpec {
    @Builder.Default
    private final JsonSchema schema = JsonSchema.ofType(STRING);
}
