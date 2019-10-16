package com.egbert.rconcise.task;

import android.app.Activity;
import android.util.Log;

import com.egbert.rconcise.download.DownloadItem;
import com.egbert.rconcise.download.RDownload;
import com.egbert.rconcise.internal.Utils;
import com.egbert.rconcise.internal.http.IRequest;
import com.egbert.rconcise.internal.http.Request;
import com.egbert.rconcise.service.DownloadServiceImpl;
import com.egbert.rconcise.service.IDownloadOrUploadReqService;
import com.egbert.rconcise.service.IReqService;
import com.egbert.rconcise.service.ReqServiceImpl;
import com.egbert.rconcise.service.UploadServiceImpl;
import com.egbert.rconcise.upload.UploadItem;

import java.lang.ref.WeakReference;

/**
 * Created by Egbert on 2/25/2019.
 */
public class ReqTask implements Runnable {
    public static final int GENERAL_REQ = 1;
    public static final int DOWNLOAD_REQ = 2;
    public static final int UPLOAD_REQ = 3;
    private int taskId;
    private WeakReference<Activity> activity;
    private IReqService reqService;
    private CustomFuturetask futureTask;
    /**
     *   GENERAL_REQ 1 ReqServiceImpl 处理的常规get post请求 <p>
     *   DOWNLOAD_REQ 2 DownloadServiceImpl 处理的下载任务 <p>
     *   UPLOAD_REQ 3 UploadServiceImpl 处理的上传任务
     */
    private int taskType;

    public ReqTask(IRequest request) {
        if (request instanceof Request) {
            this.reqService = new ReqServiceImpl();
            taskType = GENERAL_REQ;
            this.activity = new WeakReference<>(((Request) request).activity());
        } else if (request instanceof RDownload){
            this.reqService = new DownloadServiceImpl();
            taskType = DOWNLOAD_REQ;
        } else {
            this.reqService = new UploadServiceImpl();
            taskType = UPLOAD_REQ;
        }
        this.reqService.setRequest(request);
    }

    public Activity getActivity() {
        if (activity != null) {
            return activity.get();
        }
        return null;
    }

    public void setDownloadItem(DownloadItem item) {
        this.taskId = item.id;
        ((DownloadServiceImpl) this.reqService).setDownloadItem(item);
    }

    public void setRDownload(RDownload rDownload) {
        this.reqService.setRequest(rDownload);
    }

    public void setUploadItem(UploadItem item) {
        this.taskId = item.id;
        ((UploadServiceImpl) this.reqService).setUploadItem(item);
    }

    public int getTaskId() {
        return taskId;
    }

    @Override
    public void run() {
        if (activity != null && Utils.isFinishActivity(activity.get())) {
            return;
        }
        reqService.execute();
    }

    public synchronized void start() {
        if (futureTask == null) {
            futureTask = new CustomFuturetask(this, null);
        }
        ((IDownloadOrUploadReqService) reqService).resume();
        try {
            if (taskType == DOWNLOAD_REQ) {
                DownloadUploadThreadPoolManager.getInst().executeDownload(futureTask);
            } else if (taskType == UPLOAD_REQ){
                DownloadUploadThreadPoolManager.getInst().executeUpload(futureTask);
            }
        } catch (InterruptedException e) {
            Log.e(ReqTask.class.getSimpleName(), Log.getStackTraceString(e));
            e.printStackTrace();
        }
    }

    public void pause() {
        ((IDownloadOrUploadReqService) reqService).pause();
        if (futureTask != null) {
            DownloadUploadThreadPoolManager.getInst().remove(futureTask, taskType);
            futureTask = null;
        }
    }

    /**
     * @param isDelFile  是否删除文件  true 删除 false 不删除
     */
    public void cancel(boolean isDelFile) {
        ((IDownloadOrUploadReqService) reqService).cancel(isDelFile);
        if (futureTask != null) {
            DownloadUploadThreadPoolManager.getInst().remove(futureTask, taskType);
            futureTask = null;
        }
    }

    public boolean isStart() {
        return futureTask != null || DownloadUploadThreadPoolManager.getInst().isExisted(futureTask, taskType);
    }

}
