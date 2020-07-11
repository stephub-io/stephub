package io.stephub.server.service.exception;

public class ExecutionPrerequisiteException extends ExecutionException {
    public ExecutionPrerequisiteException(String message) {
        super(message);
    }

    public ExecutionPrerequisiteException(String message, Throwable cause) {
        super(message, cause);
    }
}
