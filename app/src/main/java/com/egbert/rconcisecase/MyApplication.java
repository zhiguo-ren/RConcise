package com.egbert.rconcisecase;

import android.app.Application;

import com.egbert.rconcise.RConcise;
import com.egbert.rconcise.download.RDownloadManager;

import static com.egbert.rconcisecase.MainActivity.RCLIENT_KEY;

/**
 * Created by Egbert on 3/21/2019.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        RDownloadManager.inst().init(this);
        RConcise.inst().createRClient(RCLIENT_KEY);
    }
}
