package com.egbert.rconcise.download;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.egbert.rconcise.database.dao.RDaoFactory;
import com.egbert.rconcise.download.enums.DownloadStatus;
import com.egbert.rconcise.download.enums.Priority;
import com.egbert.rconcise.download.interfaces.IDownloadObserver;
import com.egbert.rconcise.download.interfaces.IDownloadServiceCallable;
import com.egbert.rconcise.internal.HeaderField;
import com.egbert.rconcise.internal.Utils;
import com.egbert.rconcise.task.ReqTask;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Egbert on 3/18/2019.
 */
public class RDownloadManager implements IDownloadServiceCallable {
    public static final String DEF_PATH = "rdownload";
    private static final String DB_NAME = "RDownload.db";
    private String packageName;
    private byte[] lock = new byte[0];
    private DownloadDao downloadDao;
    private SimpleDateFormat dateFormat;
    private IDownloadObserver observer;
    private List<DownloadItem> currDownloadItems = new CopyOnWriteArrayList<>();
    private Handler handler = new Handler(Looper.getMainLooper());
    private static volatile RDownloadManager sManager;
    private AtomicBoolean isInit = new AtomicBoolean(false);

    private RDownloadManager() {
    }

    public static RDownloadManager inst() {
        if (sManager == null) {
            synchronized (RDownloadManager.class) {
                if (sManager == null) {
                    sManager = new RDownloadManager();
                }
            }
        }
        return sManager;
    }

    /**
     * 初始化方法
     */
    public synchronized void init(Context context) {
        if (isInit.compareAndSet(false, true)) {
            RDaoFactory.getInst().openOrCreateDb(DB_NAME, context);
            downloadDao = RDaoFactory.getInst().getDao(DownloadDao.class, DownloadItem.class, DB_NAME);
            dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA);
            packageName = context.getPackageName();
        }
    }

    public int download(RDownload rDownload) throws UnsupportedEncodingException, IllegalStateException,
            IllegalArgumentException {
        StringBuilder path = new StringBuilder();
        DownloadItem item = new DownloadItem();
        if (!TextUtils.isEmpty(rDownload.directory())) {
            path.append(rDownload.directory());
            if (!rDownload.directory().endsWith(File.separator)) {
                path.append(File.separator);
            }
        } else {
            path.append(Environment.getExternalStorageDirectory().getAbsolutePath())
                    .append(File.separator)
                    .append(packageName)
                    .append(File.separator)
                    .append(DEF_PATH)
                    .append(File.separator);
        }
        if (!TextUtils.isEmpty(rDownload.fileName())) {
            if (!rDownload.fileName().contains(".")) {
                throw new IllegalArgumentException("The file name does not contain an extension.");
            }
            item.fileName = rDownload.fileName();
        } else {
            item.fileName = Utils.guessFileName(rDownload.url());
        }
        path.append(item.fileName);
        item.filePath = path.toString();
        item.url = rDownload.url();
        DownloadItem existedItem = downloadDao.findRecord(item.filePath);
        if (existedItem != null) {
            File file = new File(existedItem.filePath);
            if (file.exists()) {
                if (file.length() == existedItem.totalLen && existedItem.totalLen != 0) {
                    if (existedItem.status != DownloadStatus.finish.getValue()) {
                        existedItem.status = DownloadStatus.finish.getValue();
                        downloadDao.updateRecord(existedItem);
                    }
                    rDownload.observer().onError(existedItem.id, ErrorCode.EXIST, ErrorCode.EXIST.getMsg());
                    return existedItem.id;
                } else {
                    if (existedItem.totalLen != 0) {
                        RDownload.Builder builder = new RDownload.Builder(rDownload);
                        builder.addHeader(HeaderField.RANGE.getValue(), "bytes=" + (file.length() + 1) + "-");
                        rDownload = builder.build();
                    }
                    if (existedItem.currLen != file.length()) {
                        existedItem.currLen = file.length();
                    }
                    existedItem.status = DownloadStatus.waiting.getValue();
                }
            } else {
                if (existedItem.reqTask == null) {
                    existedItem.url = rDownload.url();
                    existedItem.currLen = 0;
                    existedItem.totalLen = 0;
                    existedItem.priority = Priority.HIGH.getValue();
                    existedItem.status = DownloadStatus.waiting.getValue();
                    existedItem.startTime = dateFormat.format(new Date());
                    existedItem.endTime = "0";
                }
            }
            if (existedItem.reqTask != null) {
                if (existedItem.reqTask.isStart()) {
                    rDownload.observer().onError(existedItem.id,
                            ErrorCode.DOWNLOADING, ErrorCode.DOWNLOADING.getMsg());
                } else {
                    existedItem.reqTask.start();
                }
            } else {
                existedItem.reqTask = new ReqTask(rDownload);
                existedItem.reqTask.setDownloadItem(existedItem);
                existedItem.reqTask.setDownloadDao(downloadDao);
                existedItem.reqTask.start();
            }
            downloadDao.updateRecord(existedItem);
            return existedItem.id;
        }
        item.reqTask = new ReqTask(rDownload);
        item.reqTask.setDownloadItem(existedItem);
        item.reqTask.setDownloadDao(downloadDao);
        int recrodId = downloadDao.addRecord(item);
        item.reqTask.start();
        return recrodId;
    }

    private boolean isDownloading(String absolutePath) {
        for (DownloadItem downloadItem : currDownloadItems) {
            if (downloadItem.filePath.equals(absolutePath)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDownloadStatusChanged(DownloadItem downloadItem) {

    }

    @Override
    public void onTotalLengthReceived(DownloadItem downloadItem) {

    }

    @Override
    public void onCurrProgress(DownloadItem downloadItem, double downLen, long speed) {

    }

    @Override
    public void onDownloadSuccess(DownloadItem downloadItem) {

    }

    @Override
    public void onDownloadPause(DownloadItem downloadItem) {

    }

    @Override
    public void onDownloadError(DownloadItem downloadItem, int var2, String var3) {

    }

}
