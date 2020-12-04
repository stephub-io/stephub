package io.stephub.server.api.model;

import io.stephub.json.Json;
import io.stephub.json.JsonNull;
import io.stephub.json.schema.JsonSchema;
import lombok.*;

import javax.validation.Valid;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@EqualsAndHashCode
public class Variable {
    private Json defaultValue = JsonNull.INSTANCE;
    private String description;
    @Valid
    private JsonSchema schema = JsonSchema.ofType(Json.JsonType.ANY);
}
