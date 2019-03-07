package com.egbert.rconcise.task;

import com.egbert.rconcise.internal.http.Request;
import com.egbert.rconcise.service.IReqService;
import com.egbert.rconcise.service.ReqServiceImpl;

/**
 * Created by Egbert on 2/25/2019.
 */
public class ReqTask implements Runnable {
    private IReqService reqService;

    public ReqTask(Request request) {
        this.reqService = new ReqServiceImpl();
        this.reqService.setRequest(request);
    }

    @Override
    public void run() {
        reqService.execute();
    }

}
