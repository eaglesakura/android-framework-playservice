package com.eaglesakura.android.gms.client;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import com.eaglesakura.android.gms.error.DeveloperImplementFailedException;
import com.eaglesakura.android.gms.error.PlayServiceConnectException;
import com.eaglesakura.android.gms.error.PlayServiceException;
import com.eaglesakura.android.gms.error.RequireRetryConnectException;
import com.eaglesakura.android.gms.error.SignInRequireException;
import com.eaglesakura.android.rx.error.TaskCanceledException;
import com.eaglesakura.lambda.CallbackUtils;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.thread.Holder;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import java.io.Closeable;
import java.io.IOException;

public class PlayServiceConnection implements Closeable {
    public enum Status {
        Connected,
        Suspended,
        Failed,
    }

    private Bundle mConnectedHint;

    private int mCause;

    private ConnectionResult mFailedResult;

    private final Status mStatus;

    private GoogleApiClient mClient;

    PlayServiceConnection(Bundle connectedHint) {
        mStatus = Status.Connected;
        mConnectedHint = connectedHint;
    }

    PlayServiceConnection(int cause) {
        mStatus = Status.Suspended;
        mCause = cause;
    }

    PlayServiceConnection(ConnectionResult failedResult) {
        mFailedResult = failedResult;
        mStatus = Status.Failed;
    }

    public Bundle getConnectedHint() {
        return mConnectedHint;
    }

    public int getCause() {
        return mCause;
    }

    public ConnectionResult getFailedResult() {
        return mFailedResult;
    }

    public Status getStatus() {
        return mStatus;
    }

    public boolean isConnected() {
        return mStatus == Status.Connected;
    }

    /**
     * サインインが必要な状態である場合はtrue
     */
    public boolean isRequreSignIn() {
        return mFailedResult != null
                && (mFailedResult.getErrorCode() == ConnectionResult.SIGN_IN_REQUIRED ||
                mFailedResult.getErrorCode() == ConnectionResult.SIGN_IN_FAILED);
    }

    /**
     * Google Sign Inを行うためのIntentを取得する
     */
    public Intent newSignInIntent() {
        return Auth.GoogleSignInApi.getSignInIntent(mClient);
    }

    /**
     * PlayServiceのアップデートが必要
     */
    public boolean isRequireUpdatePlayService() {
        return mFailedResult != null
                && mFailedResult.getErrorCode() == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED;
    }

    /**
     * 開発側の実装エラーである場合true
     */
    public boolean isDeveloperError() {
        return mFailedResult != null
                && mFailedResult.getErrorCode() == ConnectionResult.DEVELOPER_ERROR;
    }

    @Override
    public void close() throws IOException {
        try {
            mClient.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public GoogleApiClient getClient() {
        return mClient;
    }

    /**
     * 接続に成功した場合のみClientを取得し、それ以外は例外を投げる。
     */
    public GoogleApiClient getClientIfSuccess() throws PlayServiceException {
        try {
            if (mFailedResult != null) {
                if (isRequreSignIn()) {
                    throw new SignInRequireException(newSignInIntent());
                }

                if (isDeveloperError()) {
                    throw new DeveloperImplementFailedException(mFailedResult);
                }

                throw new PlayServiceConnectException(mFailedResult);
            }

            if (mStatus == Status.Suspended) {
                throw new RequireRetryConnectException(mCause);
            }


            return getClient();
        } catch (PlayServiceException e) {
            throw e;
        }
    }

    /**
     * APIに対して接続を行う
     *
     * @param builder        接続対象のAPI
     * @param cancelCallback キャンセルチェック
     * @return 接続済みの結果
     * @throws TaskCanceledException 接続中にキャンセルされた
     */
    public static PlayServiceConnection newInstance(GoogleApiClient.Builder builder, CancelCallback cancelCallback) throws TaskCanceledException {
        Holder<PlayServiceConnection> holder = new Holder<>();

        GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle connectionHint) {
                holder.set(new PlayServiceConnection(connectionHint));
            }

            @Override
            public void onConnectionSuspended(int cause) {
                holder.set(new PlayServiceConnection(cause));
            }
        };
        GoogleApiClient.OnConnectionFailedListener failedListener = result -> {
            holder.set(new PlayServiceConnection(result));
        };

        GoogleApiClient client = builder.build();
        try {
            client.registerConnectionCallbacks(connectionCallbacks);
            client.registerConnectionFailedListener(failedListener);

            client.connect();

            PlayServiceConnection item;
            while ((item = holder.get()) == null) {
                if (CallbackUtils.isCanceled(cancelCallback)) {
                    client.disconnect();
                    throw new TaskCanceledException();
                }
            }

            // クライアントを接続
            item.mClient = client;
            return item;
        } finally {
            client.unregisterConnectionFailedListener(failedListener);
            client.unregisterConnectionCallbacks(connectionCallbacks);
        }
    }
}
