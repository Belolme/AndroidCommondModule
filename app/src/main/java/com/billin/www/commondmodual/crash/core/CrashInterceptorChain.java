package com.billin.www.commondmodual.crash.core;


import java.util.HashMap;
import java.util.List;

/**
 * 崩溃任务链。该任务链会从传入 list 的最后一个任务一直执行到第一个。
 * <p>
 * Create by Billin on 2018/12/28
 */
public class CrashInterceptorChain implements CrashInterceptor.Chain {

    private Thread thread;

    private Throwable throwable;

    private List<CrashInterceptor> interceptors;

    private int nextIndex;

    public CrashInterceptorChain(Thread thread,
                                 Throwable throwable,
                                 List<CrashInterceptor> crashInterceptors,
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

        return interceptors.get(nextIndex)
                .interceptor(new CrashInterceptorChain(thread, throwable, interceptors, nextIndex));
    }
}
