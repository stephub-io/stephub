package io.stephub.cli.exception;

import okhttp3.Response;

public class RemoteException extends RuntimeException {
    public RemoteException(final String message) {
        super(message);
    }

    public RemoteException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
