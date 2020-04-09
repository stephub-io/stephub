package org.mbok.cucumberform.expression.model;

import lombok.AllArgsConstructor;
import org.mbok.cucumberform.expression.EvaluationContext;
import org.mbok.cucumberform.json.Json;
import org.mbok.cucumberform.json.JsonObject;

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
