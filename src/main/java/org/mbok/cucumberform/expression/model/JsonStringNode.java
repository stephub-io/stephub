package org.mbok.cucumberform.expression.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.mbok.cucumberform.expression.EvaluationContext;
import org.mbok.cucumberform.json.Json;
import org.mbok.cucumberform.json.JsonString;

@AllArgsConstructor
@Getter
public class JsonStringNode extends JsonValueNode {
    private String text;

    @Override
    public Json evaluate(EvaluationContext ec) {
        return new JsonString(text.replaceAll("\\\\\"", "\""));
    }
}
