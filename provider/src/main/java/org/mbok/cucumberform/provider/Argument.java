package org.mbok.cucumberform.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.mbok.cucumberform.json.Json;

@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode
public class Argument {
    private String name;
    private Json value;
}
