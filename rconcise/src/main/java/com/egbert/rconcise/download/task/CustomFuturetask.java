package com.egbert.rconcise.download.task;

import com.egbert.rconcise.task.ReqTask;

import java.util.concurrent.FutureTask;

/**
 * Created by Egbert on 4/16/2019.
 */
public class CustomFuturetask extends FutureTask {

    private int taskId;

    public CustomFuturetask(ReqTask runnable, Object result) {
        super(runnable, result);
        taskId = runnable.getTaskId();
    }

    public int getTaskId() {
        return taskId;
    }

}
