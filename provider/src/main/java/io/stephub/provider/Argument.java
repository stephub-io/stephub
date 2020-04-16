package io.stephub.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.stephub.json.Json;

@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode
public class Argument {
    private String name;
    private Json value;
}
