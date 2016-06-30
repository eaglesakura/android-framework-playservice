package com.eaglesakura.android.gms.error;

import com.google.android.gms.common.ConnectionResult;

/**
 * 開発者による実装エラーである場合に投げられる
 */
public class DeveloperImplementFailedException extends PlayServiceConnectException {
    public DeveloperImplementFailedException(ConnectionResult connectionResult) {
        super(connectionResult);
    }

    public DeveloperImplementFailedException(String message, ConnectionResult connectionResult) {
        super(message, connectionResult);
    }

    public DeveloperImplementFailedException(String message, Throwable cause, ConnectionResult connectionResult) {
        super(message, cause, connectionResult);
    }

    public DeveloperImplementFailedException(Throwable cause, ConnectionResult connectionResult) {
        super(cause, connectionResult);
    }
}
