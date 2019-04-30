package com.egbert.rconcisecase.model;

import com.egbert.rconcise.download.DownloadItem;
import com.egbert.rconcise.download.listener.IDownloadObserver;

/**
 * Created by Egbert on 4/18/2019.
 */
public class Download {
    public int id;
    public String url;
    public String fileName;
    public String filePath;
    public IDownloadObserver observer;
    public DownloadItem downloadItem;
    public String total;
    public boolean isStart;
    public boolean isCancel;

}
