package com.billin.www.commondmodual.utils.function;

/**
 * Create by Billin on 2019/9/18
 */
public class Assertions {

    /**
     * @throws NullPointerException Throw NullPointerException if obj is null.
     */
    public static void checkNotNull(Object obj) {
        checkNotNull(obj, null);
    }

    /**
     * @throws NullPointerException Throw NullPointerException if obj is null.
     */
    public static void checkNotNull(Object obj, String errorMsg) {
        if (obj == null) {
            throw new NullPointerException(errorMsg);
        }
    }

    /**
     * @throws IllegalStateException Throw {@link IllegalStateException} if predicate is false.
     */
    public static void check(boolean predicate) {
        check(predicate, null);
    }

    /**
     * @throws IllegalStateException Throw {@link IllegalStateException} if predicate is false.
     */
    public static void check(boolean predicate, String errorString) {
        if (!predicate) {
            throw new IllegalStateException(errorString);
        }
    }

    /**
     * @throws IllegalArgumentException Throw {@link IllegalArgumentException} if predicate is false.
     */
    public static void require(boolean predicate) {
        require(predicate, null);
    }

    /**
     * @throws IllegalArgumentException Throw {@link IllegalArgumentException} if predicate is false.
     */
    public static void require(boolean predicate, String errorString) {
        if (!predicate) {
            throw new IllegalArgumentException(errorString);
        }
    }
}
