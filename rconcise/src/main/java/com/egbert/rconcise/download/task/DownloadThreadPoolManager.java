package com.egbert.rconcise.download.task;

import com.egbert.rconcise.task.ReqTask;

import java.util.concurrent.ArrayBlockingQueue;
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

    private LinkedBlockingDeque<CustomFuturetask> deque;
    private ThreadPoolExecutor executor;

    private RejectedExecutionHandler handler = new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            try {
                deque.put(new CustomFuturetask((ReqTask) r, null));
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
                    executor.execute(futureTask);
                }
            }
        }
    };

    private DownloadThreadPoolManager() {
        deque = new LinkedBlockingDeque<>();
        executor = new ThreadPoolExecutor(5, 5, 10,
                TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(2), handler);
        executor.allowCoreThreadTimeOut(true);
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

    public synchronized void execute(CustomFuturetask task) throws InterruptedException {
        for (CustomFuturetask futuretask : deque) {
            if (futuretask.getTaskId() == task.getTaskId()) {
                return;
            }
        }
        deque.put(task);
    }

    public boolean remove(CustomFuturetask task) {
        for (CustomFuturetask futuretask : deque) {
            if (futuretask.getTaskId() == task.getTaskId()) {
                return deque.remove(futuretask);
            }
        }
        for (Runnable runnable : executor.getQueue()) {
            if (((ReqTask) runnable).getTaskId() == task.getTaskId()) {
                return executor.remove(runnable);
            }
        }
        return false;
    }

    public boolean isExisted(CustomFuturetask task) {
        for (CustomFuturetask futuretask : deque) {
            if (task.getTaskId() == futuretask.getTaskId()) {
                return true;
            }
        }
        for (Runnable runnable : executor.getQueue()) {
            if (((ReqTask) runnable).getTaskId() == task.getTaskId()) {
                return true;
            }
        }
        return false;
    }

}
