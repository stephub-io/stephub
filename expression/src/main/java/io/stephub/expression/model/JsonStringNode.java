package io.stephub.expression.model;

import io.stephub.expression.EvaluationContext;
import io.stephub.json.JsonString;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = false)
@ToString
public class JsonStringNode extends JsonValueNode<JsonString> {
    private String text;

    @Override
    public JsonString evaluate(EvaluationContext ec) {
        return new JsonString(text.replaceAll("\\\\\"", "\""));
    }
}
