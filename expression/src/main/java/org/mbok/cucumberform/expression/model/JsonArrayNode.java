package org.mbok.cucumberform.expression.model;

import lombok.AllArgsConstructor;
import org.mbok.cucumberform.expression.EvaluationContext;
import org.mbok.cucumberform.json.Json;
import org.mbok.cucumberform.json.JsonArray;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class JsonArrayNode extends JsonValueNode<Json> {
    private final List<JsonValueNode<? extends Json>> valueNodes;

    @Override
    public Json evaluate(final EvaluationContext ec) {
        return new JsonArray(this.valueNodes.stream().
                map(in -> in.evaluate(ec)).collect(Collectors.toList()));
    }
}
