package com.egbert.rconcise.download.task;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Egbert on 3/20/2019.
 * 下载任务线程池管理器
 */
public final class DownloadThreadPoolManager {
    private static volatile DownloadThreadPoolManager sManager;

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

    private DownloadThreadPoolManager() {
        deque = new LinkedBlockingDeque<>();
        executor = new ThreadPoolExecutor(3, 4, 3,
                TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(2), handler);
        executor.execute(runnable);
    }

    public static DownloadThreadPoolManager getInst() {
        if (sManager == null) {
            synchronized (DownloadThreadPoolManager.class) {
                if (sManager == null) {
                    sManager = new DownloadThreadPoolManager();
                }
            }
        }
        return sManager;
    }

    public <T> void execute(FutureTask<T> task) throws InterruptedException {
        deque.put(task);
    }

    public <T> boolean remove(FutureTask<T> task) {
        boolean result;
        if (deque.contains(task)) {
            result = deque.remove(task);
        } else {
            result = executor.remove(task);
        }
        return result;
    }

    public boolean isExisted(FutureTask task) {
        return deque.contains(task);
    }

}
