package io.stephub.provider.spec;

import io.stephub.json.schema.JsonSchema;
import lombok.*;

import java.util.List;

import static io.stephub.json.Json.JsonType.JSON;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@ToString
@EqualsAndHashCode
public class DataTableSpec {
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @ToString
    @EqualsAndHashCode
    public static class ColumnSpec {
        private String name;
        @Builder.Default
        private final JsonSchema schema = JsonSchema.ofType(JSON);
    }

    private boolean header;
    @Singular
    private List<ColumnSpec> columns;
}
