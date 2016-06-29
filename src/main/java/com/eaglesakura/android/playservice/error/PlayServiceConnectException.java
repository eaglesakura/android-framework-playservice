package com.eaglesakura.android.playservice.error;

import com.google.android.gms.common.ConnectionResult;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.IntentSender;
import android.support.annotation.Nullable;

/**
 * Google Play Serviceの接続に関連した例外
 *
 * 参考: https://developers.google.com/android/reference/com/google/android/gms/common/ConnectionResult.html
 */
public class PlayServiceConnectException extends PlayServiceException {
    final ConnectionResult mConnectionResult;

    public PlayServiceConnectException(ConnectionResult connectionResult) {
        mConnectionResult = connectionResult;
    }

    public PlayServiceConnectException(String message, ConnectionResult connectionResult) {
        super(message);
        mConnectionResult = connectionResult;
    }

    public PlayServiceConnectException(String message, Throwable cause, ConnectionResult connectionResult) {
        super(message, cause);
        mConnectionResult = connectionResult;
    }

    public PlayServiceConnectException(Throwable cause, ConnectionResult connectionResult) {
        super(cause);
        mConnectionResult = connectionResult;
    }

    public void startResolutionForResult(Activity activity, int requestCode) throws IntentSender.SendIntentException {
        mConnectionResult.startResolutionForResult(activity, requestCode);
    }

    public boolean hasResolution() {
        return mConnectionResult.hasResolution();
    }

    public boolean isSuccess() {
        return mConnectionResult.isSuccess();
    }

    public int getErrorCode() {
        return mConnectionResult.getErrorCode();
    }

    @Nullable
    public PendingIntent getResolution() {
        return mConnectionResult.getResolution();
    }

    @Nullable
    public String getErrorMessage() {
        return mConnectionResult.getErrorMessage();
    }
}
