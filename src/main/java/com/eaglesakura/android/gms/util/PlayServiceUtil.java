package com.eaglesakura.android.gms.util;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.tasks.Task;

import com.eaglesakura.android.error.NetworkNotConnectException;
import com.eaglesakura.android.gms.client.PlayServiceConnection;
import com.eaglesakura.android.gms.error.PlayServiceException;
import com.eaglesakura.android.gms.error.PlayServiceNotAvailableException;
import com.eaglesakura.android.util.AndroidNetworkUtil;
import com.eaglesakura.lambda.CallbackUtils;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.thread.Holder;
import com.eaglesakura.util.Util;

import android.accounts.Account;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
    public static Intent newSignInIntent(GoogleApiClient.Builder builder, CancelCallback cancelCallback) throws InterruptedException, PlayServiceException {
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
    public static <T extends Result> T await(PendingResult<T> task, CancelCallback cancelCallback) throws InterruptedException {
        // タスクの完了待ちを行う
        Holder<T> holder = new Holder<>();
        task.setResultCallback(result -> holder.set(result));

        T item;
        while ((item = holder.get()) == null) {
            if (CallbackUtils.isCanceled(cancelCallback)) {
                task.cancel();
                throw new InterruptedException();
            }
            Util.sleep(1);
        }

        return item;
    }

    public static <T extends Result> T await(OptionalPendingResult<T> task, CancelCallback cancelCallback) throws InterruptedException {
        while (!CallbackUtils.isCanceled(cancelCallback)) {
            if (task.isDone()) {
                return task.get();
            }
            if (task.isCanceled()) {
                throw new InterruptedException();
            }
            Util.sleep(1);
        }
        throw new InterruptedException();
    }

    /**
     * キャンセルチェックとネットワークチェックを行ったうえで処理待ちを行う
     */
    public static <T extends Result> T awaitWithNetwork(Context context, PendingResult<T> task, CancelCallback cancelCallback) throws InterruptedException, NetworkNotConnectException {
        // タスクの完了待ちを行う
        Holder<T> holder = new Holder<>();
        task.setResultCallback(result -> holder.set(result));

        T item;
        while ((item = holder.get()) == null) {
            AndroidNetworkUtil.assertNetworkConnected(context);

            if (CallbackUtils.isCanceled(cancelCallback)) {
                task.cancel();
                throw new InterruptedException();
            }

            Util.sleep(1);
        }

        return item;
    }

    /**
     * PlayService Taskの終了待ちを行う
     *
     * @throws TaskCanceledException タスクがキャンセルされた
     */
    public static <T> Task<T> await(Task<T> task, CancelCallback cancelCallback) throws InterruptedException {
        while (!task.isComplete()) {
            if (CallbackUtils.isCanceled(cancelCallback)) {
                throw new InterruptedException();
            }

            Util.sleep(1);
        }
        return task;
    }

    /**
     * PlayService Taskの終了待ちを行う
     *
     * @throws TaskCanceledException タスクがキャンセルされた
     */
    public static <T> Task<T> awaitWithNetwork(Context context, Task<T> task, CancelCallback cancelCallback) throws InterruptedException, NetworkNotConnectException {
        while (!task.isComplete()) {
            AndroidNetworkUtil.assertNetworkConnected(context);

            if (CallbackUtils.isCanceled(cancelCallback)) {
                throw new InterruptedException();
            }

            Util.sleep(1);
        }
        return task;
    }

    /**
     * 必須バージョンがインストールされているか確認する
     */
    public static boolean isInstalledRequireVersion(Context context) {
        // Google Play Serviceのバージョンチェックを行う
        GoogleApiAvailability instance = GoogleApiAvailability.getInstance();
        int playServiceError = instance.isGooglePlayServicesAvailable(context);
        return playServiceError == ConnectionResult.SUCCESS;
    }

    /**
     * 必須バージョンがインストールされていることを確認し、そうでないなら例外を投げる
     */
    public static void assertInstalledRequireVersion(Context context) throws PlayServiceNotAvailableException {
        // Google Play Serviceのバージョンチェックを行う
        GoogleApiAvailability instance = GoogleApiAvailability.getInstance();
        int playServiceError = instance.isGooglePlayServicesAvailable(context);
        if (playServiceError != ConnectionResult.SUCCESS) {
            throw new PlayServiceNotAvailableException(instance, playServiceError);
        }
    }

    /**
     * Google Play ServiceのインストールIntentを生成する
     */
    public static Intent newGooglePlayServiceInstallIntent(Context context) {
        return newGooglePlayInstallIntent(context, "com.google.android.gms");
    }

    /**
     * GooglePlayでのインストールIntentを生成する
     */
    public static Intent newGooglePlayInstallIntent(@NonNull Context context, @NonNull String packageName) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=" + packageName));
        return intent;
    }

}
