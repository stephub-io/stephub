package io.stephub.provider.spec;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@EqualsAndHashCode
@ToString
public class StepSpec {
    public enum PayloadType {
        NONE,
        DOC_STRING,
        DATA_TABLE;

        @Override
        @JsonValue
        public String toString() {
            return this.name().toLowerCase();
        }
    }

    private String id;
    private String pattern;
    private PatternType patternType = PatternType.SIMPLE;
    @Singular
    private List<ArgumentSpec> arguments;
    @Builder.Default
    private PayloadType payload = PayloadType.NONE;
    private DataTableSpec dataTable;
    private DocStringSpec docString;
}
