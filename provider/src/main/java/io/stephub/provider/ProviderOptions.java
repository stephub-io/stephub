package io.stephub.provider;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.stephub.json.JsonObject;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Duration;

@NoArgsConstructor
@AllArgsConstructor
@Data
@SuperBuilder
@EqualsAndHashCode
public
class ProviderOptions {
    @Builder.Default
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Duration sessionTimeout = Duration.ofMinutes(5);
    private JsonObject options;
}
