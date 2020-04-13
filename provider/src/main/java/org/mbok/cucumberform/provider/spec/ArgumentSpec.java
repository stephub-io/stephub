package org.mbok.cucumberform.provider.spec;

import lombok.*;
import org.mbok.cucumberform.json.Json;
import org.mbok.cucumberform.json.Json.JsonType;

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
