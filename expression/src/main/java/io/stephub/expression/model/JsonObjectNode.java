package io.stephub.expression.model;

import io.stephub.expression.EvaluationContext;
import io.stephub.expression.EvaluationException;
import io.stephub.json.Json;
import io.stephub.json.JsonNumber;
import io.stephub.json.JsonObject;
import io.stephub.json.JsonString;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class JsonObjectNode extends JsonValueNode<JsonObject> {
    private final Map<JsonValueNode<? extends Json>, JsonValueNode<? extends Json>> fields;

    @Override
    public JsonObject evaluate(final EvaluationContext ec) {
        final Map<String, Json> evaluatedFields = new HashMap<>(this.fields.size());
        this.fields.forEach((keyNode, valueNode) ->
                evaluatedFields.put(this.getKey(ec, keyNode), valueNode.evaluate(ec)));
        return new JsonObject(evaluatedFields);
    }

    private String getKey(final EvaluationContext ec, final JsonValueNode<? extends Json> keyNode) {
        final Json key = keyNode.evaluate(ec);
        if (key instanceof JsonString) {
            return ((JsonString) key).getValue();
        } else if (key instanceof JsonNumber) {
            return ((JsonNumber) key).getValue().toString();
        }
        throw new EvaluationException("Invalid key \"" + key.asJsonString() + "\", expected string or number");
    }

}
