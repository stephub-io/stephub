package io.stephub.provider.spec;

import io.stephub.json.Json;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@ToString
@EqualsAndHashCode
public class DocStringSpec {
    private Json.JsonType type;
}
