package com.egbert.rconcise.task;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.egbert.rconcise.task.ReqTask.UPLOAD_REQ;

/**
 * Created by Egbert on 3/20/2019.
 * 下载/上传任务线程池管理器
 */
public final class DownloadAndUploadThreadPoolManager {
    private static volatile DownloadAndUploadThreadPoolManager sManager;

    private LinkedBlockingDeque<CustomFuturetask> downloadDeque;
    private LinkedBlockingDeque<CustomFuturetask> uploadDeque;
    private ThreadPoolExecutor downloadExecutor;
    private ThreadPoolExecutor uploadExecutor;
    private AtomicBoolean isLaunchDownload = new AtomicBoolean(false);
    private AtomicBoolean isLaunchUpload = new AtomicBoolean(false);
    private AtomicBoolean isInitAloneUpload = new AtomicBoolean(false);
    /**
     * 是否给上传任务开启独立的线程池，true是，false否（默认false） 和下载任务共用线程池<p>
     * 建议如果要实现大量的批量上传操作，专门做上传/下载类的app，如网盘类app，开启独立上传线程池；<p>
     * 如果只是常规上传操作，例如购物类app的评价图片上传，办公类app上传文件，文档等，
     * 此类弱上传类app无需开启独立的上传线程池，节省系统资源；
     */
    private boolean isAloneUpload;

    private RejectedExecutionHandler handlerDownload = new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            try {
                downloadDeque.put(new CustomFuturetask((ReqTask) r, null));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    private RejectedExecutionHandler handlerUpload = new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            try {
                uploadDeque.put(new CustomFuturetask((ReqTask) r, null));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    private Runnable runnableDownload = new Runnable() {
        @Override
        public void run() {
            while (true) {
                CustomFuturetask futureTask = null;
                try {
                    if (isLaunchDownload.get()) {
                        futureTask = downloadDeque.take();
                    } else {
                        return;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (futureTask != null) {
                    downloadExecutor.execute(futureTask);
                }
            }
        }
    };

    private Runnable runnableUpload = new Runnable() {
        @Override
        public void run() {
            while (true) {
                CustomFuturetask futureTask = null;
                try {
                    if (isLaunchUpload.get()) {
                        futureTask = uploadDeque.take();
                    } else {
                        return;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (futureTask != null) {
                    uploadExecutor.execute(futureTask);
                }
            }
        }
    };

    private DownloadAndUploadThreadPoolManager() {
        downloadDeque = new LinkedBlockingDeque<>();
        downloadExecutor = new ThreadPoolExecutor(5, 5, 10,
                TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(2), handlerDownload);
        downloadExecutor.allowCoreThreadTimeOut(true);
        isLaunchDownload.compareAndSet(false, true);
        downloadExecutor.execute(runnableDownload);
    }

    private void createAloneUpload() {
        if (isAloneUpload && isInitAloneUpload.compareAndSet(false, true)) {
            uploadDeque = new LinkedBlockingDeque<>();
            uploadExecutor = new ThreadPoolExecutor(5, 5, 10,
                    TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(2), handlerUpload);
            uploadExecutor.allowCoreThreadTimeOut(true);
            isLaunchUpload.compareAndSet(false, true);
            uploadExecutor.execute(runnableUpload);
        }
    }

    public static DownloadAndUploadThreadPoolManager getInst() {
        if (sManager == null) {
            synchronized (DownloadAndUploadThreadPoolManager.class) {
                if (sManager == null) {
                    sManager = new DownloadAndUploadThreadPoolManager();
                }
            }
        }
        return sManager;
    }

    /**
     * 重新启动线程池，开始阻塞式从队列中取任务执行，如不需要再执行任务，可调用<code>terminateDownload()<code/>方法；
     */
    public void launchDownload() {
        isLaunchDownload.compareAndSet(false, true);
        downloadExecutor.execute(runnableDownload);
    }

    /**
     * 终止线程池工作，不在从队列取任务执行，停止工作，如需再次开启，可调用<code>launchDownload()<code/>方法；
     */
    public void terminateDownload() {
        isLaunchDownload.compareAndSet(true, false);
    }

    /**
     * 重新启动上传线程池，开始阻塞式从队列中取任务执行，如不需要再执行任务，可调用<code>terminateUpload()<code/>方法；
     */
    public void launchUpload() {
        isLaunchUpload.compareAndSet(false, true);
        uploadExecutor.execute(runnableUpload);
    }

    /**
     * 终止上传线程池工作，不在从队列取任务执行，停止工作，如需再次开启，可调用<code>launchUpload()<code/>方法；
     */
    public void terminateUpload() {
        isLaunchUpload.compareAndSet(true, false);
    }

    /**
     * 添加下载任务到任务执行队列
     * @param task 下载任务
     */
    public synchronized void executeDownload(CustomFuturetask task) throws InterruptedException {
        for (CustomFuturetask futuretask : downloadDeque) {
            if (futuretask.getTaskId() == task.getTaskId()) {
                return;
            }
        }
        downloadDeque.put(task);
    }

    /**
     * 添加上传任务到任务执行队列<p>
     * 如果开启独立的上传线程池，添加到uploadDeque中,否则添加到downloadDeque中，和下载共享线程池；
     * @param task  上传任务
     */
    public synchronized void executeUpload(CustomFuturetask task) throws InterruptedException {
        LinkedBlockingDeque<CustomFuturetask> tmp;
        if (isAloneUpload) {
            tmp = uploadDeque;
        } else {
            tmp = downloadDeque;
        }
        for (CustomFuturetask futuretask : tmp) {
            if (futuretask.getTaskId() == task.getTaskId()) {
                return;
            }
        }
        tmp.put(task);
    }

    public boolean remove(CustomFuturetask task, int taskType) {
        for (CustomFuturetask futuretask : downloadDeque) {
            if (futuretask.getTaskId() == task.getTaskId()) {
                return downloadDeque.remove(futuretask);
            }
        }
        for (Runnable runnable : downloadExecutor.getQueue()) {
            if (((ReqTask) runnable).getTaskId() == task.getTaskId()) {
                return downloadExecutor.remove(runnable);
            }
        }
        if (taskType == UPLOAD_REQ && isAloneUpload) {
            for (CustomFuturetask futuretask : uploadDeque) {
                if (futuretask.getTaskId() == task.getTaskId()) {
                    return uploadDeque.remove(futuretask);
                }
            }
            for (Runnable runnable : uploadExecutor.getQueue()) {
                if (((ReqTask) runnable).getTaskId() == task.getTaskId()) {
                    return uploadExecutor.remove(runnable);
                }
            }
        }
        return false;
    }

    public boolean isExisted(CustomFuturetask task, int taskType) {
        for (CustomFuturetask futuretask : downloadDeque) {
            if (task.getTaskId() == futuretask.getTaskId()) {
                return true;
            }
        }
        for (Runnable runnable : downloadExecutor.getQueue()) {
            if (((ReqTask) runnable).getTaskId() == task.getTaskId()) {
                return true;
            }
        }
        if (taskType == UPLOAD_REQ && isAloneUpload) {
            for (CustomFuturetask futuretask : uploadDeque) {
                if (futuretask.getTaskId() == task.getTaskId()) {
                    return true;
                }
            }
            for (Runnable runnable : uploadExecutor.getQueue()) {
                if (((ReqTask) runnable).getTaskId() == task.getTaskId()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isAloneUpload() {
        return isAloneUpload;
    }

    /**
     * 如需开启独立上传线程池，需要调用该方法并赋值为true 如：<code>setAloneUpload(true)<code/>
     * @param aloneUpload 是否开启独立上传线程池
     */
    public void setAloneUpload(boolean aloneUpload) {
        isAloneUpload = aloneUpload;
        createAloneUpload();
    }
}
