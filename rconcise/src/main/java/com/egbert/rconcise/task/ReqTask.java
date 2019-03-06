package com.egbert.rconcise.task;

import com.egbert.rconcise.internal.http.Request;

/**
 * Created by Egbert on 2/25/2019.
 */
public class ReqTask implements Runnable {
    private Request request;

    public ReqTask(Request request) {
        this.request = request;
    }

    @Override
    public void run() {
        request.reqService().execute();
    }

}
