package com.billin.www.commondmodual.utils.aysnc;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class AsyncJobTest {

    @Test
    public void toFuture() {
        HandlerThread handlerThread = new HandlerThread("AsyncJobTest");
        handlerThread.start();
        Handler handler = new Handler(handlerThread.getLooper());

        handler.post(() -> {
            System.out.println("hello: start");
            Future<String> job = AsyncJob.just(0b1111_1111_1111_1111)
                    .flatMap(input -> new AsyncJob<Integer>() {
                        @Override
                        public void start(AsyncJobResultCallback<Integer> callback) {
                            // Deadlock!!!
                            // Handler newHandler = new Handler();

                            Handler newHandler = new Handler(Looper.getMainLooper());
                            newHandler.post(() -> callback.onResult(input));
                        }
                    })
                    .map(Integer::bitCount)
                    .map(bitCount -> {
                        System.out.println("hello: execute map in " + Thread.currentThread());
                        return String.valueOf(bitCount);
                    })
                    .toFuture();
            try {
                String result = job.get();
                System.out.println("hello: result " + result);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }

            release();
        });

        await();
    }

    private synchronized void await() {
        try {
            AsyncJobTest.this.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private synchronized void release() {
        synchronized (AsyncJobTest.this) {
            AsyncJobTest.this.notifyAll();
        }
    }
}