package com.billin.www.commondmodual.utils.aysnc;

/**
 * Create by Billin on 2019/12/6
 *
 * @param <E> 成功回调类型
 */
public interface AsyncJobResultCallback<E> {

    void onResult(E result);

    void onError(Throwable throwable);
}
