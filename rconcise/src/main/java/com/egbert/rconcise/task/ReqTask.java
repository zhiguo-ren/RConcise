package com.egbert.rconcise.task;

import android.util.Log;

import com.egbert.rconcise.download.DownloadItem;
import com.egbert.rconcise.download.RDownload;
import com.egbert.rconcise.download.task.CustomFuturetask;
import com.egbert.rconcise.download.task.DownloadThreadPoolManager;
import com.egbert.rconcise.internal.http.IRequest;
import com.egbert.rconcise.internal.http.Request;
import com.egbert.rconcise.service.DownloadServiceImpl;
import com.egbert.rconcise.service.IReqService;
import com.egbert.rconcise.service.ReqServiceImpl;

/**
 * Created by Egbert on 2/25/2019.
 */
public class ReqTask implements Runnable {
    private int taskId;
    private IReqService reqService;
    private CustomFuturetask futureTask;

    public ReqTask(IRequest request) {
        if (request instanceof Request) {
            this.reqService = new ReqServiceImpl();
        } else {
            this.reqService = new DownloadServiceImpl();
        }
        this.reqService.setRequest(request);
    }

    public void setDownloadItem(DownloadItem item) {
        this.taskId = item.id;
        ((DownloadServiceImpl) this.reqService).setDownloadItem(item);
    }

    public void setRDownload(RDownload rDownload) {
        this.reqService.setRequest(rDownload);
    }

    public int getTaskId() {
        return taskId;
    }

    @Override
    public void run() {
        reqService.execute();
    }

    public synchronized void start() {
        if (futureTask == null) {
            futureTask = new CustomFuturetask(this, null);
        }
        ((DownloadServiceImpl) reqService).resume();
        try {
            DownloadThreadPoolManager.getInst().execute(futureTask);
        } catch (InterruptedException e) {
            Log.e(ReqTask.class.getSimpleName(), Log.getStackTraceString(e));
        }
    }

    public void pause() {
        ((DownloadServiceImpl) reqService).pause();
        if (futureTask != null) {
            DownloadThreadPoolManager.getInst().remove(futureTask);
            futureTask = null;
        }
    }

    /**
     * @param isDelFile  是否删除文件  true 删除 false 不删除
     */
    public void cancel(boolean isDelFile) {
        ((DownloadServiceImpl) reqService).cancel(isDelFile);
        if (futureTask != null) {
            DownloadThreadPoolManager.getInst().remove(futureTask);
            futureTask = null;
        }
    }

    public boolean isStart() {
        return futureTask != null || DownloadThreadPoolManager.getInst().isExisted(futureTask);
    }

}
