package com.billin.www.library.crash.core;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * 异常处理器
 * <p>
 * Create by Billin on 2018/12/28
 */
public interface CrashInterceptor {
    HashMap<String, String> interceptor(@NotNull Chain chain);

    interface Chain {
        Thread getThread();

        Throwable getThrowable();

        HashMap<String, String> process();
    }
}
