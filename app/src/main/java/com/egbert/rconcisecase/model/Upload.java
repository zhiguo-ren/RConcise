package com.egbert.rconcisecase.model;

import com.egbert.rconcise.upload.UploadItem;
import com.egbert.rconcise.upload.listener.IUploadObserver;

/**
 * Created by Egbert on 4/28/2019.
 */
public class Upload {
    public int id;
    public String url;
    public String fileName;
    public String filePath;
    public IUploadObserver observer;
    public UploadItem uploadItem;
    public String total;
    public boolean isNewTask;
    public boolean isStart;
    public boolean isCancel;
}
