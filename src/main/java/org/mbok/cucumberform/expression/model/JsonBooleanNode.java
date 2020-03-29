package org.mbok.cucumberform.expression.model;

import lombok.AllArgsConstructor;
import org.mbok.cucumberform.expression.EvaluationContext;
import org.mbok.cucumberform.json.Json;
import org.mbok.cucumberform.json.JsonBoolean;

@AllArgsConstructor
public class JsonBooleanNode extends JsonValueNode {
    private boolean value;

    @Override
    public Json evaluate(EvaluationContext ec) {
        return new JsonBoolean(value);
    }
}
