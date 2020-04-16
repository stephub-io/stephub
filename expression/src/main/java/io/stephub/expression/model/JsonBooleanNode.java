package io.stephub.expression.model;

import lombok.AllArgsConstructor;
import io.stephub.expression.EvaluationContext;
import io.stephub.json.JsonBoolean;

@AllArgsConstructor
public class JsonBooleanNode extends JsonValueNode<JsonBoolean> {
    private boolean value;

    @Override
    public JsonBoolean evaluate(EvaluationContext ec) {
        return new JsonBoolean(value);
    }
}
