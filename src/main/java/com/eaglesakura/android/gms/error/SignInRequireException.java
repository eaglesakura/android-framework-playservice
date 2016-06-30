package com.eaglesakura.android.gms.error;

import android.content.Intent;

/**
 * ユーザーのSign Inが
 */
public class SignInRequireException extends PlayServiceException {
    final Intent mSignInIntent;

    public SignInRequireException(Intent signInIntent) {
        mSignInIntent = signInIntent;
    }

    public SignInRequireException(String message, Intent signInIntent) {
        super(message);
        mSignInIntent = signInIntent;
    }

    public SignInRequireException(String message, Throwable cause, Intent signInIntent) {
        super(message, cause);
        mSignInIntent = signInIntent;
    }

    public SignInRequireException(Throwable cause, Intent signInIntent) {
        super(cause);
        mSignInIntent = signInIntent;
    }

    /**
     * サインインを行うためのIntentを取得する
     */
    public Intent getSignInIntent() {
        return mSignInIntent;
    }
}
