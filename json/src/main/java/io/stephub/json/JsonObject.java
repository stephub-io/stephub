package io.stephub.json;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@SuperBuilder
public class JsonObject extends Json {
    @Singular
    private Map<String, Json> fields = new HashMap<>();

    /**
     * @param key in fields
     * @return value in fields if existing, otherwise {@link JsonNull}
     */
    public Json getOpt(final String key) {
        final Json value = this.fields.get(key);
        if (value == null) {
            return JsonNull.INSTANCE;
        }
        return value;
    }

    @Override
    public String asJsonString(final boolean pretty) {
        final StringBuilder json = new StringBuilder();
        final String global_separator;
        final String pair_prefix;
        final String pair_separator;
        final String value_prefix;
        if (pretty) {
            global_separator = "\n";
            pair_prefix = "  ";
            pair_separator = ",\n";
            value_prefix = " ";
        } else {
            global_separator = "";
            pair_prefix = "";
            pair_separator = ",";
            value_prefix = "";
        }
        final StringBuilder str = new StringBuilder("{").append(global_separator);
        boolean sep = false;
        for (final Map.Entry<String, Json> entry : this.fields.entrySet()) {
            if (sep) {
                str.append(pair_separator);
            }
            str.append(pair_prefix).append("\"").append(this.encodeString(entry.getKey()))
                    .append("\":").append(value_prefix).append(entry.getValue().asJsonString(pretty));
            sep = true;
        }
        str.append(global_separator).append("}");
        return str.toString();
    }

}
