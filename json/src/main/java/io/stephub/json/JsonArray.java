package io.stephub.json;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Builder
public class JsonArray extends Json {
    @Singular
    private List<Json> values;

    @Override
    public String asJsonString(final boolean pretty) {
        final String global_separator;
        final String item_prefix;
        final String item_separator;
        if (pretty) {
            global_separator = "\n";
            item_prefix = "  ";
            item_separator = ",\n";
        } else {
            global_separator = " ";
            item_prefix = "";
            item_separator = ", ";
        }
        final StringBuilder s = new StringBuilder("[").append(global_separator);
        boolean sep = false;
        for (final Json i : this.values) {
            if (sep) {
                s.append(item_separator);
            }
            s.append(item_prefix).append(i.asJsonString(pretty));
            sep = true;
        }
        s.append(global_separator).append("]");
        return s.toString();
    }

    @Override
    public JsonType getType() {
        return JsonType.ARRAY;
    }

    public Json getOpt(final int index) {
        final Json v = this.values.get(index);
        return v != null ? v : new JsonNull();
    }
}
