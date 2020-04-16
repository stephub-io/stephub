package io.stephub.expression.model;

import io.stephub.expression.EvaluationContext;
import io.stephub.json.JsonNull;

public class JsonNullNode extends JsonValueNode<JsonNull> {
    @Override
    public JsonNull evaluate(EvaluationContext ec) {
        return new JsonNull();
    }
}
