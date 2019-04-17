package com.billin.www.commondmodual.crash.core;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 异常处理的总控制器。其设计借鉴了 OKHttp interceptor 的设计。
 * <p>
 * Create by Billin on 2018/12/28
 */
public class CrashHandler {

    private static CrashHandler mInstance;

    private static Thread.UncaughtExceptionHandler SYSTEM_CRASH_HANDLER;

    private List<CrashInterceptor> mInterceptors;

    public static CrashHandler getInstance() {
        if (mInstance == null) {
            synchronized (CrashHandler.class) {
                if (mInstance == null) mInstance = new CrashHandler();
            }
        }

        return mInstance;
    }

    private CrashHandler() {

        mInterceptors = new ArrayList<>();

        Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(final Thread t, final Throwable e) {
                synchronized (CrashHandler.this) {
                    if (e == null || mInterceptors.isEmpty()) {
                        SYSTEM_CRASH_HANDLER.uncaughtException(t, e);
                        return;
                    }

                    CrashInterceptorChain chain = new CrashInterceptorChain(t,
                            e, mInterceptors, mInterceptors.size());
                    try {
                        chain.process();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        SYSTEM_CRASH_HANDLER.uncaughtException(t, e);
                    }
                }
            }
        };

        SYSTEM_CRASH_HANDLER = Thread.getDefaultUncaughtExceptionHandler();
        if (SYSTEM_CRASH_HANDLER == null) {
            SYSTEM_CRASH_HANDLER = new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(1);
                }
            };
        }
        Thread.setDefaultUncaughtExceptionHandler(handler);
    }

    public synchronized void addInterceptor(CrashInterceptor interceptor) {
        mInterceptors.add(interceptor);
    }

    public synchronized void removeInterceptor(CrashInterceptor interceptor) {
        mInterceptors.remove(interceptor);
    }

    public CrashInterceptor getSystemDefaultInterceptor() {
        return new SystemDefaultInterceptor();
    }

    /**
     * 系统默认的崩溃处理，其行为为打印日志到控制台中并强制结束进程退出应用。
     */
    private static class SystemDefaultInterceptor implements CrashInterceptor {
        @Override
        public HashMap<String, String> interceptor(@NotNull CrashInterceptor.Chain next) {
            HashMap<String, String> res = next.process();
            SYSTEM_CRASH_HANDLER.uncaughtException(next.getThread(), next.getThrowable());
            return res;
        }
    }
}
