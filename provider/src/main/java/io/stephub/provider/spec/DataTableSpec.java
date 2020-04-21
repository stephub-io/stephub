package io.stephub.provider.spec;

import io.stephub.json.Json.JsonType;
import lombok.*;

import java.util.List;

@AllArgsConstructor
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
        private JsonType type;
    }

    private final boolean hasHeader;
    @Singular
    private final List<ColumnSpec> columns;
}
