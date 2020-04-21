package io.stephub.provider;

import io.stephub.json.Json;
import lombok.*;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Data
@EqualsAndHashCode
@Builder
public class StepRequest {
    private String id;
    @Singular
    private Map<String, Json> arguments;
    private Json docString;
    private List<Map<String, Json>> dataTable;
}
