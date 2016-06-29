package com.eaglesakura.android.framework.playservice.error;

/**
 * 何らかの理由で接続が不完全になったため、リトライを要求する
 */
public class RequireRetryConnectException extends PlayServiceException {
    final int mPlayServiceCause;

    public RequireRetryConnectException(int playServiceCause) {
        mPlayServiceCause = playServiceCause;
    }

    public RequireRetryConnectException(String message, int playServiceCause) {
        super(message);
        mPlayServiceCause = playServiceCause;
    }

    public RequireRetryConnectException(String message, Throwable cause, int playServiceCause) {
        super(message, cause);
        mPlayServiceCause = playServiceCause;
    }

    public RequireRetryConnectException(Throwable cause, int playServiceCause) {
        super(cause);
        mPlayServiceCause = playServiceCause;
    }

    public int getPlayServiceCause() {
        return mPlayServiceCause;
    }
}
