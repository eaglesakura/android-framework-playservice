package com.eaglesakura.android.gms.error;

public class PlayServiceException extends Exception {

    public PlayServiceException() {
    }

    public PlayServiceException(String message) {
        super(message);
    }

    public PlayServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public PlayServiceException(Throwable cause) {
        super(cause);
    }
}
