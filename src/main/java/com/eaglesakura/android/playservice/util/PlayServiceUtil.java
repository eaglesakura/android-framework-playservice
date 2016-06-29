package com.eaglesakura.android.playservice.util;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;

import com.eaglesakura.android.framework.FwLog;
import com.eaglesakura.android.playservice.client.PlayServiceConnection;
import com.eaglesakura.android.playservice.error.PlayServiceException;
import com.eaglesakura.android.rx.error.TaskCanceledException;
import com.eaglesakura.lambda.CallbackUtils;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.thread.Holder;

import android.annotation.SuppressLint;
import android.content.Intent;

import java.io.IOException;

/**
 * PlayService系のUtil
 */
public class PlayServiceUtil {

    /**
     * ログインを行うためのIntentを発行する。
     *
     * 既にログイン済みの場合、アクセスを一旦revokeして再度ログインを促すようにする
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
            Status revokeAccess = PlayServiceUtil.await(Auth.GoogleSignInApi.revokeAccess(client), cancelCallback);
            FwLog.google("Revoke Access[%s]", revokeAccess.getStatus().toString());

            return connection.newSignInIntent();
        } catch (IOException e) {
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
}
