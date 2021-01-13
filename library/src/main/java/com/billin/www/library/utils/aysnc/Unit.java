package com.billin.www.library.utils.aysnc;

/**
 * Callback 中表示无返回值
 * <p>
 * Create by Billin on 2019/12/6
 */
public class Unit {
    private static Unit sInstance;

    private Unit() {
    }

    public static Unit getInstance() {
        if (sInstance == null) {
            synchronized (Unit.class) {
                if (sInstance == null) {
                    sInstance = new Unit();
                }
            }
        }

        return sInstance;
    }
}
