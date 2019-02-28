package com.egbert.rconcise.task;

import android.util.Log;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Egbert on 2/26/2019.
 * 线程池管理器
 */
public final class ThreadPoolManager {
    public static final String TAG = "Concise";
    private static ThreadPoolManager sManager;

    private LinkedBlockingDeque<Future<?>> deque;
    private ThreadPoolExecutor executor;

    private RejectedExecutionHandler handler = new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            try {
                deque.put(new FutureTask<>(r, null));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                FutureTask futureTask = null;
                try {
                    Log.e(TAG, "提取任务前 " + deque.size());
                    futureTask = (FutureTask) deque.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (futureTask != null) {
                    executor.execute(futureTask);
                    Log.e(TAG, "提取任务后 " + deque.size());
                }
            }
        }
    };

    private ThreadPoolManager() {
        deque = new LinkedBlockingDeque<>();
        executor = new ThreadPoolExecutor(4, Runtime.getRuntime().availableProcessors() * 2, 5,
                TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(Runtime.getRuntime().availableProcessors() * 2), handler);
        executor.execute(runnable);
    }

    public static ThreadPoolManager getInst() {
        if (sManager == null) {
            sManager = new ThreadPoolManager();
        }
        return sManager;
    }

    public void execute(Runnable runnable) throws InterruptedException {
        deque.put(new FutureTask<>(runnable, null));
    }

}
