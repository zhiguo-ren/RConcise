package com.egbert.rconcise.task;

import com.egbert.rconcise.internal.Utils;

import java.util.concurrent.ArrayBlockingQueue;
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

    private LinkedBlockingDeque<CustomFuturetask> deque;
    private ThreadPoolExecutor executor;

    private RejectedExecutionHandler handler = new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            try {
                deque.put((CustomFuturetask) r);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                CustomFuturetask futureTask = null;
                try {
                    futureTask = deque.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (futureTask != null) {
                    if (!Utils.isFinishActivity(futureTask.getActivity())) {
                        executor.execute(futureTask);
                    }
                }
            }
        }
    };

    private ThreadPoolManager() {
        deque = new LinkedBlockingDeque<>();
        int num = Runtime.getRuntime().availableProcessors() * 10;
        executor = new ThreadPoolExecutor(8, 8, 20,
                TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(num),
                new CustomThreadFactory("normal"), handler);
        executor.allowCoreThreadTimeOut(true);
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

    public void execute(ReqTask runnable) throws InterruptedException {
        deque.put(new CustomFuturetask(runnable, null));
    }

}
