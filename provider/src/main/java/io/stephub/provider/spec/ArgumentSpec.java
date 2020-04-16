package io.stephub.provider.spec;

import lombok.*;
import io.stephub.json.Json.JsonType;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@EqualsAndHashCode
@ToString
public class ArgumentSpec {
    private String name;
    private JsonType type;
}
