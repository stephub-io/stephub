package org.mbok.cucumberform.testspec;

import org.mbok.cucumberform.json.Json;

public class PayloadDataTable implements PayloadSpec {
    public static class RowSpec {
        private boolean headline;
    }

    public static class ColSpec {
        private Json.JsonType type;
    }
}
