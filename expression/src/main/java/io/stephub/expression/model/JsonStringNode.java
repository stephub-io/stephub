package io.stephub.expression.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import io.stephub.expression.EvaluationContext;
import io.stephub.json.JsonString;

@AllArgsConstructor
@Getter
public class JsonStringNode extends JsonValueNode<JsonString> {
    private String text;

    @Override
    public JsonString evaluate(EvaluationContext ec) {
        return new JsonString(text.replaceAll("\\\\\"", "\""));
    }
}
