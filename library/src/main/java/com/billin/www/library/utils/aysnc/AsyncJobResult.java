package com.billin.www.library.utils.aysnc;

/**
 * Create by Billin on 2019/12/31
 */
public class AsyncJobResult<T> {

    private T result;

    private Throwable throwable;

    public AsyncJobResult(T result, Throwable throwable) {
        this.result = result;
        this.throwable = throwable;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public String toString() {
        return "AsyncJobResult{" +
                "result=" + result +
                ", throwable=" + throwable +
                '}';
    }
}
