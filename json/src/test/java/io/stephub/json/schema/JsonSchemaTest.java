package io.stephub.json.schema;

import io.stephub.json.Json;
import io.stephub.json.JsonBoolean;
import io.stephub.json.JsonNumber;
import io.stephub.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
public class JsonSchemaTest {

    @Test
    public void testValidJSON() {
        final int e = 10000;
        final long start = System.currentTimeMillis();
        for (int i = 0; i < e; i++) {
            JsonSchema.ofType(Json.JsonType.BOOLEAN).accept(JsonBoolean.valueOf(Math.random() > 0.5));
        }
        log.info("Boolean validation for {} times in {}ms with {}ms per validation", e, System.currentTimeMillis() - start, (double) (System.currentTimeMillis() - start) / e);
    }

    @Test
    public void testInvalidJSON() {
        final JsonInvalidSchemaException e = assertThrows(JsonInvalidSchemaException.class, () -> {
            JsonSchema.ofType(Json.JsonType.BOOLEAN).accept(new JsonObject());
        });
        log.debug("Expected invalid JSON", e);
    }

    @Test
    public void testInvalidSchema() {
        final JsonSchema schema = new JsonSchema();
        schema.getFields().put("type", new JsonNumber(5));
        final JsonSchema.SchemaValidity validity = schema.validate();
        assertThat(validity.isValid(), equalTo(false));
        assertThat(validity.getErrors(), hasSize(2));
        assertThat(validity.getErrors(), hasItem("$.type: does not have a value in the enumeration [array, boolean, integer, null, number, object, string]"));
    }
}