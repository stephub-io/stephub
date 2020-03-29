package org.mbok.cucumberform.json;

public abstract class Json {
    public abstract String asString();

    @Override
    public String toString() {
        return asString();
    }
}
