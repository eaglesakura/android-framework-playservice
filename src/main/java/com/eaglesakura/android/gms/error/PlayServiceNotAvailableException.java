package com.eaglesakura.android.gms.error;

import com.google.android.gms.common.GoogleApiAvailability;

/**
 * Google Play Serviceの接続に関連した例外
 *
 * 参考: https://developers.google.com/android/reference/com/google/android/gms/common/ConnectionResult.html
 */
public class PlayServiceNotAvailableException extends PlayServiceException {
    GoogleApiAvailability mApiAvailability;

    int mErrorCode;

    public PlayServiceNotAvailableException(GoogleApiAvailability apiAvailability, int errorCode) {
        mApiAvailability = apiAvailability;
        mErrorCode = errorCode;
    }

    public String getErrorMessage() {
        return mApiAvailability.getErrorString(mErrorCode);
    }
}
