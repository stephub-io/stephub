package io.stephub.cli.exception;

public class RemoteException extends RuntimeException {
    public RemoteException(final String message) {
        super(message);
    }

    public RemoteException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
