package com.eaglesakura.android.gms.util;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.tasks.Task;

import com.eaglesakura.android.error.NetworkNotConnectException;
import com.eaglesakura.android.gms.client.PlayServiceConnection;
import com.eaglesakura.android.gms.error.PlayServiceException;
import com.eaglesakura.android.rx.error.TaskCanceledException;
import com.eaglesakura.android.util.AndroidNetworkUtil;
import com.eaglesakura.lambda.CallbackUtils;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.thread.Holder;
import com.eaglesakura.util.Util;

import android.accounts.Account;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;

/**
 * PlayService系のUtil
 */
public class PlayServiceUtil {

    /**
     * ログインを行うためのIntentを発行する。
     *
     * 既にログイン済みの場合、アクセスを一旦signOutして再度ログインを促すようにする
     *
     * @param builder        ログイン対象のAPI
     * @param cancelCallback キャンセルチェック
     * @return ログイン用intent
     */
    @SuppressLint("NewApi")
    public static Intent newSignInIntent(GoogleApiClient.Builder builder, CancelCallback cancelCallback) throws TaskCanceledException, PlayServiceException {
        try (
                PlayServiceConnection connection = PlayServiceConnection.newInstance(builder, cancelCallback)
        ) {
            GoogleApiClient client = connection.getClientIfSuccess();
//            await(Auth.GoogleSignInApi.revokeAccess(client), cancelCallback);
            await(Auth.GoogleSignInApi.signOut(client), cancelCallback);
            return connection.newSignInIntent();
        } catch (IOException e) {
            throw new PlayServiceException(e);
        }
    }

    /**
     * OAuth2アクセストークンを取得する
     *
     * @param email       UserEmail
     * @param scope       アクセススコープ
     * @param extraScopes 2個以上のアクセススコープを指定する場合
     */
    @Nullable
    public static String getAccessToken(@NonNull Context context, @NonNull String email, @NonNull String scope, String... extraScopes) throws PlayServiceException {
        try {
            return GoogleAuthUtil.getToken(
                    context,
                    new Account(email, "com.google"),
                    "oauth2:" + scope
            );
        } catch (Exception e) {
            throw new PlayServiceException(e);
        }
    }

    /**
     * キャンセルチェックを行ったうえで処理待ちを行う
     */
    public static <T extends Result> T await(PendingResult<T> task, CancelCallback cancelCallback) throws TaskCanceledException {
        // タスクの完了待ちを行う
        Holder<T> holder = new Holder<>();
        task.setResultCallback(result -> holder.set(result));

        T item;
        while ((item = holder.get()) == null) {
            if (CallbackUtils.isCanceled(cancelCallback)) {
                task.cancel();
                throw new TaskCanceledException();
            }
            Util.sleep(1);
        }

        return item;
    }

    public static <T extends Result> T await(OptionalPendingResult<T> task, CancelCallback cancelCallback) throws TaskCanceledException {
        while (!CallbackUtils.isCanceled(cancelCallback)) {
            if (task.isDone()) {
                return task.get();
            }
            if (task.isCanceled()) {
                throw new TaskCanceledException();
            }
            Util.sleep(1);
        }
        throw new TaskCanceledException();
    }

    /**
     * キャンセルチェックとネットワークチェックを行ったうえで処理待ちを行う
     */
    public static <T extends Result> T awaitWithNetwork(Context context, PendingResult<T> task, CancelCallback cancelCallback) throws TaskCanceledException, NetworkNotConnectException {
        // タスクの完了待ちを行う
        Holder<T> holder = new Holder<>();
        task.setResultCallback(result -> holder.set(result));

        T item;
        while ((item = holder.get()) == null) {
            AndroidNetworkUtil.assertNetworkConnected(context);

            if (CallbackUtils.isCanceled(cancelCallback)) {
                task.cancel();
                throw new TaskCanceledException();
            }
        }

        return item;
    }

    /**
     * PlayService Taskの終了待ちを行う
     *
     * @throws TaskCanceledException タスクがキャンセルされた
     */
    public static <T> Task<T> await(Task<T> task, CancelCallback cancelCallback) throws TaskCanceledException {
        while (!task.isComplete()) {
            if (CallbackUtils.isCanceled(cancelCallback)) {
                throw new TaskCanceledException();
            }
        }
        return task;
    }

    /**
     * PlayService Taskの終了待ちを行う
     *
     * @throws TaskCanceledException タスクがキャンセルされた
     */
    public static <T> Task<T> awaitWithNetwork(Context context, Task<T> task, CancelCallback cancelCallback) throws TaskCanceledException, NetworkNotConnectException {
        while (!task.isComplete()) {
            AndroidNetworkUtil.assertNetworkConnected(context);

            if (CallbackUtils.isCanceled(cancelCallback)) {
                throw new TaskCanceledException();
            }
        }
        return task;
    }
}
