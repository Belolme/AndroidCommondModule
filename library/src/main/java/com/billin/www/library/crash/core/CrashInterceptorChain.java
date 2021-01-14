package com.billin.www.library.crash.core;


import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.List;

/**
 * 崩溃任务链。该任务链会从传入 list 的最后一个任务一直执行到第一个。
 * <p>
 * Create by Billin on 2018/12/28
 */
class CrashInterceptorChain implements CrashInterceptor.Chain {

    private final Thread thread;

    private final Throwable throwable;

    private final List<CrashInterceptor> interceptors;

    private final int nextIndex;

    CrashInterceptorChain(
            Thread thread,
            Throwable throwable,
            @NonNull List<CrashInterceptor> crashInterceptors,
            int interceptorIndex) {
        this.thread = thread;
        this.throwable = throwable;
        this.interceptors = crashInterceptors;
        this.nextIndex = interceptorIndex - 1;
    }

    @Override
    public Thread getThread() {
        return thread;
    }

    @Override
    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public HashMap<String, String> process() {
        if (nextIndex < 0) return new HashMap<>();

        return interceptors.get(nextIndex).interceptor(new CrashInterceptorChain(
                thread, throwable, interceptors, nextIndex
        ));
    }
}
