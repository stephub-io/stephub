package io.stephub.expression.model;

import lombok.AllArgsConstructor;
import io.stephub.expression.EvaluationContext;
import io.stephub.json.Json;
import io.stephub.json.JsonArray;

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
