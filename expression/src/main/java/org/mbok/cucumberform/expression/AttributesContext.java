package org.mbok.cucumberform.expression;

import org.mbok.cucumberform.json.Json;

public interface AttributesContext {
    Json get(String key);
}
