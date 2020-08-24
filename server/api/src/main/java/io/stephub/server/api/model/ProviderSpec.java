package io.stephub.server.api.model;

import io.stephub.json.JsonObject;
import io.stephub.provider.api.model.ProviderOptions;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public class ProviderSpec extends ProviderOptions<JsonObject> implements Identifiable {
    @NotEmpty
    private String name;

    @Pattern(regexp = "\\s*(>|>=|=|$)\\s*\\d+(\\.\\d+(\\.\\d+)?)?")
    private String version;

    @URL
    private String providerUrl;

    @Override
    public JsonObject getOptions() {
        final JsonObject opt = super.getOptions();
        if (opt == null) {
            return new JsonObject();
        }
        return opt;
    }

    @Override
    public String getId() {
        return this.name;
    }
}
