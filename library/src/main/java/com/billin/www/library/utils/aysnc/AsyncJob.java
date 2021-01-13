package com.billin.www.library.utils.aysnc;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;
import androidx.core.util.Consumer;

import com.billin.www.library.utils.function.BiFunction;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 一个简单的异步工具类, 类似于 RxJava 中的 Single 类.
 * 尽最大努力减少地狱般的 callback 嵌套回调.
 * 虽然这个东西在不是一条流的调用下还是会不可避免的产生地狱嵌套.
 * <p>
 * Create by Billin on 2019/12/6
 *
 * @param <E> 成功回调类型
 */
@SuppressWarnings("Convert2Lambda")
public abstract class AsyncJob<E> {

    private static final String TAG = "AsyncJob";

    public static <T> AsyncJob<T> just(@NonNull final T param) {
        return new AsyncJob<T>() {
            @Override
            public void start(AsyncJobResultCallback<T> callback) {
                callback.onResult(param);
            }
        };
    }

    public abstract void start(AsyncJobResultCallback<E> callback);

    public void start(@Nullable final Consumer<E> onResult, @Nullable final Consumer<Throwable> onError) {
        start(new AsyncJobResultCallback<E>() {
            @Override
            public void onResult(E result) {
                if (onResult != null) onResult.accept(result);
            }

            @Override
            public void onError(Throwable throwable) {
                if (onError != null) onError.accept(throwable);
            }
        });
    }

    public <R> AsyncJob<R> map(@NonNull final Function<E, R> fun) {
        return new AsyncJob<R>() {
            @Override
            public void start(final AsyncJobResultCallback<R> callback) {
                AsyncJob.this.start(new AsyncJobResultCallback<E>() {
                    @Override
                    public void onResult(E input) {
                        try {
                            R result = fun.apply(input);
                            callback.onResult(result);
                        } catch (Throwable e) {
                            callback.onError(e);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        callback.onError(throwable);
                    }
                });
            }
        };
    }

    public <R> AsyncJob<R> flatMap(@NonNull final Function<E, AsyncJob<R>> fun) {
        return new AsyncJob<R>() {
            @Override
            public void start(final AsyncJobResultCallback<R> callback) {
                AsyncJob.this.start(new AsyncJobResultCallback<E>() {
                    @Override
                    public void onResult(E input) {
                        try {
                            AsyncJob<R> resultJob = fun.apply(input);
                            resultJob.start(new AsyncJobResultCallback<R>() {
                                @Override
                                public void onResult(R result) {
                                    callback.onResult(result);
                                }

                                @Override
                                public void onError(Throwable throwable) {
                                    callback.onError(throwable);
                                }
                            });
                        } catch (Throwable e) {
                            callback.onError(e);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        callback.onError(throwable);
                    }
                });
            }
        };
    }

    public <T, R> AsyncJob<R> zip(@NonNull final AsyncJob<T> other, @NonNull final BiFunction<E, T, R> transform) {
        final AsyncJob<E> sourceJob = this;
        return new AsyncJob<R>() {
            @Override
            @SuppressWarnings("unchecked")
            public void start(final AsyncJobResultCallback<R> callback) {
                final List<AsyncJob<?>> asyncJobs = Arrays.asList(sourceJob, other);
                final AtomicBoolean errorCall = new AtomicBoolean(false);
                final AtomicInteger resultCall = new AtomicInteger(asyncJobs.size());
                final Object[] results = new Object[asyncJobs.size()];

                final Consumer<Throwable> onError = new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        if (!errorCall.getAndSet(true)) {
                            callback.onError(throwable);
                        } else {
                            Log.e(TAG, "onError: ", throwable);
                        }
                    }
                };

                final Consumer<Unit> onResult = new Consumer<Unit>() {
                    @Override
                    public void accept(Unit unit) {
                        int remainCall = resultCall.decrementAndGet();
                        if (remainCall == 0) {
                            try {
                                callback.onResult(transform.apply(
                                        (E) results[0],
                                        (T) results[1]
                                ));
                            } catch (Throwable e) {
                                callback.onError(e);
                            }
                        }
                    }
                };


                for (int i = 0; i < asyncJobs.size(); i++) {
                    final int resultIndex = i;
                    asyncJobs.get(i).start(new AsyncJobResultCallback() {
                        @Override
                        public void onResult(Object result) {
                            results[resultIndex] = result;
                            onResult.accept(Unit.getInstance());
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            onError.accept(throwable);
                        }
                    });
                }
            }
        };
    }

    /**
     * 切换到主线程. 调用 {@link #start(AsyncJobResultCallback)} 方法后,
     * 其中的 Callback 将会在主线程中回调.
     */
    public AsyncJob<E> switchMainThread() {
        throw new UnsupportedOperationException();
    }

    /**
     * 切换到异步线程. 调用 {@link #start(AsyncJobResultCallback)} 方法后,
     * 其中的 Callback 将会在异步线程中回调.
     */
    public AsyncJob<E> switchAsyncThread() {
        throw new UnsupportedOperationException();
    }

    /**
     * 把异步任务转换成同步任务. 如果有切换线程的操作需要注意死锁.
     */
    public Future<E> toFuture() {
        FutureAsyncJobResultCallback<E> callback = new FutureAsyncJobResultCallback<>();
        start(callback);
        return callback;
    }
}
