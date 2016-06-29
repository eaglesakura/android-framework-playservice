package com.eaglesakura.android.framework.playservice.util;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;

import com.eaglesakura.android.rx.error.TaskCanceledException;
import com.eaglesakura.lambda.CallbackUtils;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.thread.Holder;

/**
 * PlayService系のUtil
 */
public class PlayServiceUtil {
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
}
