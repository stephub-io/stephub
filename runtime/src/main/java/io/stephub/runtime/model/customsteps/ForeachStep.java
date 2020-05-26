package io.stephub.runtime.model.customsteps;

import io.stephub.expression.EvaluationContext;
import io.stephub.expression.ExpressionEvaluator;
import io.stephub.json.*;
import io.stephub.provider.api.model.StepResponse;
import io.stephub.runtime.model.NestedStepResponse;
import io.stephub.runtime.service.SessionExecutionContext;
import io.stephub.runtime.validation.ExpressionValidator;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.stephub.runtime.util.ErrorMessageBeautifier.wrapJson;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ForeachStep extends BasicStep {
    @ExpressionValidator.Valid
    private String itemsExpression;
    private final String indexAttributeName = "index";
    private final String valueAttributeName = "value";

    @Override
    protected NestedStepResponse executeInternally(final SessionExecutionContext sessionExecutionContext, final EvaluationContext evaluationContext, final StepExecutionResolverWrapper stepExecutionResolver, final ExpressionEvaluator expressionEvaluator) {
        final Json rawItems = expressionEvaluator.evaluate(this.itemsExpression, evaluationContext);
        final List<Pair<Json, Json>> items = new ArrayList<>();
        if (rawItems instanceof JsonArray) {
            int i = 0;
            for (final Json v : ((JsonArray) rawItems).getValues()) {
                items.add(Pair.of(new JsonNumber(i), v));
                i++;
            }
        } else if (rawItems instanceof JsonObject) {
            for (final Map.Entry<String, Json> entry : ((JsonObject) rawItems).getFields().entrySet()) {
                items.add(Pair.of(new JsonString(entry.getKey()), entry.getValue()));
            }
        } else {
            return NestedStepResponse.builder().errorMessage("Wrong type for items to iterate for: " + wrapJson(rawItems)).status(StepResponse.StepStatus.ERRONEOUS).build();
        }
        final NestedStepResponse.NestedStepResponseBuilder<?, ?> response = NestedStepResponse.builder();
        for (final Pair<Json, Json> item : items) {
            final NestedStepResponse.Context.ContextBuilder context = NestedStepResponse.Context.
                    builder().name("Loop for item: " + wrapJson(item.getValue()));
            evaluationContext.put(this.indexAttributeName, item.getKey());
            evaluationContext.put(this.valueAttributeName, item.getValue());
            this.executeNestedInstructions(sessionExecutionContext, evaluationContext, stepExecutionResolver, context, this.getInstructions());
            response.subResponse(context.build());
        }
        return response.build();
    }
}
