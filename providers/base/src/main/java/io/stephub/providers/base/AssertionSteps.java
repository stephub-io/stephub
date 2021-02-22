package io.stephub.providers.base;

import io.stephub.json.Json;
import io.stephub.json.JsonBoolean;
import io.stephub.provider.util.StepFailedException;
import io.stephub.provider.util.spring.annotation.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


@Component
@Slf4j
public class AssertionSteps {
    @StepMethod(pattern = "assert that all are truthy:", provider = BaseProvider.class,
            description = "Use this step to verify multiple JSON expressions to be truthy")
    public void assertAllTruthy(@StepDataTable(header = false,
            description = "A single-column table with a JSON expression per row",
            columns = @StepColumn(name = "condition", type = JsonBoolean.class, strict = true,
                    doc = @StepDoc(description = "A JSON expression evaluating to a boolean",
                            examples = {
                                    @StepDoc.StepDocExample(value = "true", description = "A simple boolean value"),
                                    @StepDoc.StepDocExample(value = "${var1} == ${var2}", description = "An equals condition"),
                            }))) final List<Map<String, JsonBoolean>> dataTable) {
        if (dataTable != null) {
            for (int i = 0; i < dataTable.size(); i++) {
                final Map<String, JsonBoolean> row = dataTable.get(i);
                if (!row.get("condition").isTrue()) {
                    throw new StepFailedException("Expected <" + JsonBoolean.TRUE + "> at row " + i + ", but was <" + row.get("condition") + ">");
                }
            }
        }
    }

    @StepMethod(pattern = "assert that {actual} is true", provider = BaseProvider.class)
    public void assertTrue(@StepArgument(name = "actual", strict = true) final JsonBoolean actual) {
        this.assertEquals(JsonBoolean.TRUE, actual);
    }

    @StepMethod(pattern = "assert that {actual} equals {expected}", provider = BaseProvider.class)
    public void assertTrue(@StepArgument(name = "actual") final Json actual,
                           @StepArgument(name = "expected") final Json expected) {
        this.assertEquals(expected, actual);
    }

    private void assertEquals(final Json expected, final Json actual) {
        if (!expected.equals(actual)) {
            throw new StepFailedException("Expected <" + expected + ">, but was <" + actual + ">");
        }
    }
}
