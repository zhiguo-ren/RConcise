package com.egbert.rconcise.task;

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
    private static volatile ThreadPoolManager sManager;

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
                    futureTask = (FutureTask) deque.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (futureTask != null) {
                    executor.execute(futureTask);
                }
            }
        }
    };

    private ThreadPoolManager() {
        deque = new LinkedBlockingDeque<>();
        int num = Runtime.getRuntime().availableProcessors() * 2;
        executor.allowCoreThreadTimeOut(true);
        executor = new ThreadPoolExecutor(8, 8, 20,
                TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(num),
                new CustomThreadFactory("normal"), handler);
        executor.execute(runnable);
    }

    public static ThreadPoolManager getInst() {
        if (sManager == null) {
            synchronized (ThreadPoolManager.class) {
                if (sManager == null) {
                    sManager = new ThreadPoolManager();
                }
            }
        }
        return sManager;
    }

    public void execute(Runnable runnable) throws InterruptedException {
        deque.put(new FutureTask<>(runnable, null));
    }

}
