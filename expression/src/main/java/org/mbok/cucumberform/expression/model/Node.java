package org.mbok.cucumberform.expression.model;

import org.mbok.cucumberform.expression.EvaluationContext;
import org.mbok.cucumberform.json.Json;

public abstract class Node<J extends Json> {
    public abstract J evaluate(EvaluationContext ec);
}
