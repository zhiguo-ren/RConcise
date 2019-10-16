package com.egbert.rconcise.task;

import android.app.Activity;

import java.lang.ref.WeakReference;
import java.util.concurrent.FutureTask;

/**
 * Created by Egbert on 4/16/2019.
 */
public class CustomFuturetask extends FutureTask {

    private int taskId;
    private WeakReference<Activity> activity;

    public CustomFuturetask(ReqTask runnable, Object result) {
        super(runnable, result);
        taskId = runnable.getTaskId();
        activity = new WeakReference<>(runnable.getActivity());
    }

    public int getTaskId() {
        return taskId;
    }

    public Activity getActivity() {
        if (activity != null) {
            return activity.get();
        }
        return null;
    }
}
