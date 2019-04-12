package com.egbert.rconcise.task;

import android.util.Log;

import com.egbert.rconcise.download.DownloadDao;
import com.egbert.rconcise.download.DownloadItem;
import com.egbert.rconcise.download.task.DownloadThreadPoolManager;
import com.egbert.rconcise.internal.http.IRequest;
import com.egbert.rconcise.internal.http.Request;
import com.egbert.rconcise.service.DownloadServiceImpl;
import com.egbert.rconcise.service.IReqService;
import com.egbert.rconcise.service.ReqServiceImpl;

import java.util.concurrent.FutureTask;

/**
 * Created by Egbert on 2/25/2019.
 */
public class ReqTask implements Runnable {
    private IReqService reqService;
    private FutureTask futureTask;

    public ReqTask(IRequest request) {
        if (request instanceof Request) {
            this.reqService = new ReqServiceImpl();
        } else {
            this.reqService = new DownloadServiceImpl();
        }
        this.reqService.setRequest(request);
    }

    public void setDownloadItem(DownloadItem item) {
        ((DownloadServiceImpl) this.reqService).setDownloadItem(item);
    }

    public void setDownloadDao(DownloadDao dao) {
        ((DownloadServiceImpl) this.reqService).setDownloadDao(dao);
    }

    @Override
    public void run() {
        reqService.execute();
    }

    public synchronized void start() {
        if (futureTask == null) {
            futureTask = new FutureTask(this, null);
        }
        try {
            if (!isStart()) {
                DownloadThreadPoolManager.getInst().execute(futureTask);
            }
        } catch (InterruptedException e) {
            Log.e(ReqTask.class.getSimpleName(), Log.getStackTraceString(e));
        }
    }

    public void pause() {
        ((DownloadServiceImpl) reqService).pause();
        if (futureTask != null) {
            DownloadThreadPoolManager.getInst().remove(futureTask);
        }
    }

    public boolean isStart() {
        return futureTask != null && DownloadThreadPoolManager.getInst().isExisted(futureTask);
    }

}
