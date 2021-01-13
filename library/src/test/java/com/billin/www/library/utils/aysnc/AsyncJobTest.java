package com.billin.www.library.utils.aysnc;

import org.junit.Test;

public class AsyncJobTest {

    @Test
    public void map() {
        AsyncJob.just(1)
                .map(input -> "Hello")
                .start(System.out::println, Throwable::printStackTrace);
    }

    @Test
    public void flatMap() {
        AsyncJob.just(152)
                .flatMap(input -> AsyncJob.just(input).map(Integer::bitCount))
                .start(System.out::println, Throwable::printStackTrace);
    }

    @Test
    public void zip() {
        AsyncJob<Integer> a = AsyncJob.just(2);
        AsyncJob<Integer> b = AsyncJob.just(3);
        a.zip(b, Integer::sum)
                .start(System.out::println, Throwable::printStackTrace);
    }
}