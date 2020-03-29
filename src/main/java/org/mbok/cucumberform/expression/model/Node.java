package org.mbok.cucumberform.expression.model;

import org.mbok.cucumberform.expression.EvaluationContext;
import org.mbok.cucumberform.json.Json;

public abstract class Node {
    public abstract Json evaluate(EvaluationContext ec);
}
