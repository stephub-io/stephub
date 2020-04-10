package org.mbok.cucumberform.json;

import lombok.*;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Builder
public class JsonObject extends Json {
    @Singular
    private Map<String, Json> fields = new HashMap<>();

    /**
     * @param key in fields
     * @return value in fields if existing, otherwise {@link JsonNull}
     */
    public Json getOpt(final String key) {
        final Json value = fields.get(key);
        if (value == null) {
            return new JsonNull();
        }
        return value;
    }

    @Override
    public String asJsonString(final boolean pretty) {
        final StringBuilder json = new StringBuilder();
        final String global_separator;
        final String pair_prefix;
        final String pair_separator;
        if (pretty) {
            global_separator = "\n";
            pair_prefix = "  ";
            pair_separator = ",\n";
        } else {
            global_separator = " ";
            pair_prefix = "";
            pair_separator = ", ";
        }
        final StringBuilder str = new StringBuilder("{").append(global_separator);
        boolean sep = false;
        for (final Map.Entry<String, Json> entry : fields.entrySet()) {
            if (sep) {
                str.append(pair_separator);
            }
            str.append(pair_prefix).append("\"").append(encodeString(entry.getKey()))
                    .append("\": ").append(entry.getValue().asJsonString(pretty));
            sep = true;
        }
        str.append(global_separator).append("}");
        return str.toString();
    }

    @Override
    public JsonType getType() {
        return JsonType.OBJECT;
    }
}