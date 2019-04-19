package com.egbert.rconcise.download;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.egbert.rconcise.database.dao.RDaoFactory;
import com.egbert.rconcise.download.enums.DownloadStatus;
import com.egbert.rconcise.download.enums.Priority;
import com.egbert.rconcise.internal.HeaderField;
import com.egbert.rconcise.internal.Utils;
import com.egbert.rconcise.task.ReqTask;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Egbert on 3/18/2019.
 */
public class RDownloadManager {
    public static final String DEF_PATH = "rdownload";
    private static final String DB_NAME = "RDownload.db";
    private String packageName;
    private DownloadDao downloadDao;
    private SimpleDateFormat dateFormat;
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
     * 在主activity或者application 等入口出调用，且应用只需调用一次
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

    public synchronized int download(RDownload rDownload) throws UnsupportedEncodingException, IllegalStateException,
            IllegalArgumentException {
        StringBuilder path = new StringBuilder();
        DownloadItem item = new DownloadItem();
        path.append(Environment.getExternalStorageDirectory().getAbsolutePath());
        String dir = rDownload.directory();
        if (!TextUtils.isEmpty(dir)) {
            if (dir.startsWith(File.separator)) {
                path.append(dir);
            } else {
                path.append(File.separator)
                        .append(dir);
            }
            if (!dir.endsWith(File.separator)) {
                path.append(File.separator);
            }
        } else {
            path.append(File.separator)
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
                        builder.addHeader(HeaderField.RANGE.getValue(), "bytes=" + file.length() + "-");
                        rDownload = builder.build();
                    }
                    if (existedItem.currLen != file.length()) {
                        existedItem.currLen = file.length();
                    }
                }
            } else {
                if (existedItem.reqTask == null) {
                    existedItem.url = rDownload.url();
                    existedItem.currLen = 0L;
                    existedItem.totalLen = 0L;
                    existedItem.priority = Priority.HIGH.getValue();
                    existedItem.startTime = dateFormat.format(new Date());
                    existedItem.endTime = "0";
                }
            }
            if (existedItem.reqTask != null) {
                if (existedItem.status == DownloadStatus.waiting.getValue()
                    || existedItem.status == DownloadStatus.starting.getValue()
                    || existedItem.status == DownloadStatus.downloading.getValue()) {
                    rDownload.observer().onError(existedItem.id,
                            ErrorCode.DOWNLOADING, ErrorCode.DOWNLOADING.getMsg());
                } else {
                    existedItem.reqTask.setRDownload(rDownload);
                    existedItem.status = DownloadStatus.waiting.getValue();
                    existedItem.reqTask.start();
                }
            } else {
                existedItem.reqTask = new ReqTask(rDownload);
                existedItem.reqTask.setDownloadItem(existedItem);
                existedItem.status = DownloadStatus.waiting.getValue();
                existedItem.reqTask.start();
            }
            downloadDao.updateRecord(existedItem);
            return existedItem.id;
        }
        int recrodId = downloadDao.addRecord(item);
        if (recrodId == -1) {
            rDownload.observer().onError(item.id, ErrorCode.INSER_DB_FAILED, ErrorCode.INSER_DB_FAILED.getMsg());
        } else {
            item.reqTask = new ReqTask(rDownload);
            item.reqTask.setDownloadItem(item);
            item.reqTask.start();
        }
        return recrodId;
    }

    public void pause(int downloadId) {
        DownloadItem item = downloadDao.findRecordByIdFromCached(downloadId);
        if (item != null && item.reqTask != null) {
            item.reqTask.pause();
        }
    }

    /**
     * @param downloadId 下载任务id
     * @param isDelFile 是否删除已下载的文件  true 删除  false 不删除
     */
    public void cancel(int downloadId, boolean isDelFile) {
        DownloadItem item = downloadDao.findRecordById(downloadId);
        if (item != null) {
            if (item.reqTask != null) {
                item.reqTask.cancel(isDelFile);
            } else {
                if (isDelFile) {
                    File file = new File(item.filePath);
                    if (file.exists()) {
                        file.delete();
                    }
                }
                downloadDao.delRecord(downloadId);
            }
        }
    }

    /**
     * @return 返回download 数据库 操作类 可进行增删改查操作
     */
    public DownloadDao getDownloadDao() {
        return downloadDao;
    }

    /**
     * 根据id查询下载实体bean
     * @param downloadId 下载任务id
     * @return 返回下载实体对象
     */
    public DownloadItem queryById(int downloadId) {
        return downloadDao.findRecordById(downloadId);
    }

}
