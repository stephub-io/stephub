package org.mbok.cucumberform.expression.model;

import org.mbok.cucumberform.expression.EvaluationContext;
import org.mbok.cucumberform.json.Json;
import org.mbok.cucumberform.json.JsonNull;

public class JsonNullNode extends JsonValueNode<JsonNull> {
    @Override
    public JsonNull evaluate(EvaluationContext ec) {
        return new JsonNull();
    }
}
