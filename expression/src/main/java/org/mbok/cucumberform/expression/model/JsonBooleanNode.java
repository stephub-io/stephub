package org.mbok.cucumberform.expression.model;

import lombok.AllArgsConstructor;
import org.mbok.cucumberform.expression.EvaluationContext;
import org.mbok.cucumberform.json.Json;
import org.mbok.cucumberform.json.JsonBoolean;

@AllArgsConstructor
public class JsonBooleanNode extends JsonValueNode<JsonBoolean> {
    private boolean value;

    @Override
    public JsonBoolean evaluate(EvaluationContext ec) {
        return new JsonBoolean(value);
    }
}
