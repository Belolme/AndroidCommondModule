package com.billin.www.commondmodual.utils.aysnc;


import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Create by Billin on 2019/12/31
 */
public class FutureAsyncJobResultCallback<T> implements Future<T>, AsyncJobResultCallback<T> {

    private CountDownLatch latch = new CountDownLatch(1);
    private volatile T result;
    private volatile Throwable throwable;
    private volatile boolean isCancel;

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (isDone()) {
            return false;
        }

        isCancel = true;
        latch.countDown();
        return true;
    }

    @Override
    public boolean isCancelled() {
        return isCancel;
    }

    @Override
    public boolean isDone() {
        return latch.getCount() == 0;
    }

    @Override
    public T get()
            throws ExecutionException,
            InterruptedException {
        if (latch.getCount() == 1) {
            latch.await();
        }

        if (isCancelled()) {
            throw new CancellationException();
        }

        Throwable error = throwable;
        if (error != null) {
            throw new ExecutionException(error);
        }

        return result;
    }

    @Override
    public T get(long timeout, TimeUnit unit)
            throws ExecutionException,
            InterruptedException,
            TimeoutException {
        if (latch.getCount() == 1) {
            if (!latch.await(timeout, unit)) {
                throw new TimeoutException();
            }
        }

        if (isCancelled()) {
            throw new CancellationException();
        }

        Throwable error = throwable;
        if (error != null) {
            throw new ExecutionException(error);
        }

        return result;
    }

    @Override
    public void onResult(T result) {
        this.result = result;
        latch.countDown();
    }

    @Override
    public void onError(Throwable throwable) {
        this.throwable = throwable;
        latch.countDown();
    }
}
