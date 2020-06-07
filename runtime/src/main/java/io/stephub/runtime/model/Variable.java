package io.stephub.runtime.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private Json value = JsonNull.INSTANCE;
    @JsonProperty("default")
    private Json defaultValue = JsonNull.INSTANCE;
    private String description;
    @Valid
    private JsonSchema schema = JsonSchema.ofType(Json.JsonType.ANY);
}
