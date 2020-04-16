package io.stephub.expression.model;

import lombok.AllArgsConstructor;
import io.stephub.expression.EvaluationContext;
import io.stephub.json.Json;
import io.stephub.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class JsonObjectNode extends JsonValueNode<JsonObject> {
    private final Map<JsonStringNode, JsonValueNode<?>> fields;

    @Override
    public JsonObject evaluate(final EvaluationContext ec) {
        final Map<String, Json> evaluatedFields = new HashMap<>(fields.size());
        fields.forEach((keyNode, valueNode) ->
                evaluatedFields.put(keyNode.evaluate(ec).getValue(), valueNode.evaluate(ec)));
        return new JsonObject(evaluatedFields);
    }
}
