package com.billin.www.commondmodual.utils.aysnc;

/**
 * 一个封装了 errorCode(int), errorMsg(String) 的错误异常.
 * <p>
 * Create by Billin on 2019/12/6
 */
public class AsyncJobErrorException extends RuntimeException {

    private int errorCode;

    public AsyncJobErrorException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
