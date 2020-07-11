package io.stephub.server.api.util;

import io.stephub.json.Json;

public class ErrorMessageBeautifier {
    public static String wrapJson(final Json value) {
        return "<json>" + value + "</json>";
    }
}
