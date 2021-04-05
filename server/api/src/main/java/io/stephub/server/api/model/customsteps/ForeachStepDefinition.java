package io.stephub.server.api.model.customsteps;

import io.stephub.expression.EvaluationContext;
import io.stephub.expression.ExpressionEvaluator;
import io.stephub.json.*;
import io.stephub.server.api.SessionExecutionContext;
import io.stephub.server.api.model.StepResponseContext;
import io.stephub.server.api.util.ErrorMessageBeautifier;
import io.stephub.server.api.validation.ValidExpression;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ForeachStepDefinition extends BasicStepDefinition {
    @ValidExpression
    private String itemsExpression;
    private final String indexAttributeName = "index";
    private final String valueAttributeName = "value";

    @Override
    protected void executeInternally(final SessionExecutionContext sessionExecutionContext, final EvaluationContext evaluationContext, final StepExecutionResolverWrapper stepExecutionResolver, final ExpressionEvaluator expressionEvaluator,
                                     final StepResponseContext responseContext) {
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
            throw new RuntimeException("Wrong type for items to iterate for: " + ErrorMessageBeautifier.wrapJson(rawItems));
        }
        final StepResponseContext.NestedResponseContext nestedResponseContext = responseContext.nested();
        for (final Pair<Json, Json> item : items) {
            final StepResponseContext.NestedResponseSequenceContext loopGroup = nestedResponseContext.group(Optional.of("Loop for item: " + ErrorMessageBeautifier.wrapJson(item.getValue())));
            evaluationContext.put(this.indexAttributeName, item.getKey());
            evaluationContext.put(this.valueAttributeName, item.getValue());
            this.executeNestedSteps(sessionExecutionContext, evaluationContext, stepExecutionResolver, loopGroup, this.getSteps());
        }
    }
}
