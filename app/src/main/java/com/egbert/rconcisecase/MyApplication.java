package com.egbert.rconcisecase;

import android.app.Application;

import com.egbert.rconcise.download.RDownloadManager;

/**
 * Created by Egbert on 3/21/2019.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        RDownloadManager.inst().init(this);

    }
}
