package org.mbok.cucumberform.expression.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.mbok.cucumberform.expression.EvaluationContext;
import org.mbok.cucumberform.json.JsonNumber;
import org.mbok.cucumberform.json.JsonString;

@AllArgsConstructor
@Getter
public class JsonNumberNode extends JsonValueNode<JsonNumber> {
    private Number value;

    @Override
    public JsonNumber evaluate(EvaluationContext ec) {
        return new JsonNumber(value);
    }
}
