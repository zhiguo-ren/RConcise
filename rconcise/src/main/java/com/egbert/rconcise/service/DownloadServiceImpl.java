package com.egbert.rconcise.service;

import android.text.TextUtils;
import android.util.Log;

import com.egbert.rconcise.download.DownloadDao;
import com.egbert.rconcise.download.DownloadItem;
import com.egbert.rconcise.download.ErrorCode;
import com.egbert.rconcise.download.RDownload;
import com.egbert.rconcise.download.RDownloadManager;
import com.egbert.rconcise.download.enums.DownloadStatus;
import com.egbert.rconcise.download.interfaces.DownloadListenerImpl;
import com.egbert.rconcise.download.interfaces.IDownloadListener;
import com.egbert.rconcise.internal.HeaderField;
import com.egbert.rconcise.internal.ReqMethod;
import com.egbert.rconcise.internal.Utils;
import com.egbert.rconcise.internal.http.IRequest;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Egbert on 3/19/2019.
 */
public class DownloadServiceImpl implements IReqService {
    private RDownload rDownload;
    private DownloadItem downloadItem;
    private DownloadDao downloadDao;
    private IDownloadListener respListener;
    private AtomicBoolean isPause = new AtomicBoolean(false);
    private AtomicBoolean isCancel = new AtomicBoolean(false);
    private AtomicBoolean isFirstProgress = new AtomicBoolean(false);
    private boolean isDelFile;

    @Override
    public void setRequest(IRequest request) {
        rDownload = (RDownload) request;
        respListener = new DownloadListenerImpl(rDownload.observer());
        downloadDao = RDownloadManager.inst().getDownloadDao();
    }

    public void setDownloadItem(DownloadItem item) {
        this.downloadItem = item;
    }

    @Override
    public void execute() {
        HttpURLConnection connection = null;
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        try {
            connection = (HttpURLConnection) new URL(rDownload.url()).openConnection();
            String method = rDownload.method().toUpperCase();
            boolean isPost = ReqMethod.POST.getMethod().equals(method);
            connection.setRequestMethod(method);
            connection.setConnectTimeout(20000);
            if (isPost) {
                connection.setDoOutput(true);
            }
            HashMap<String, String> headers = rDownload.headers();
            String contentType = null;
            if (headers != null) {
                contentType = headers.get(HeaderField.CONTENT_TYPE.getValue());
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    if (!TextUtils.isEmpty(entry.getValue())) {
                        connection.setRequestProperty(entry.getKey(), entry.getValue());
                    }
                }
            }
            byte[] params;
            HashMap<String, String> paramsMap = rDownload.params();
            params = Utils.paramsToByte(contentType, paramsMap);
            if (isPause() || isCancel()) {
                return;
            }
            connection.connect();
            if (isPost && params != null) {
                BufferedOutputStream writer = new BufferedOutputStream(connection.getOutputStream());
                writer.write(params);
                writer.close();
            }
            int code = connection.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK || code == HttpURLConnection.HTTP_PARTIAL) {
                downloadItem.status = DownloadStatus.starting.getValue();
                downloadDao.updateRecord(downloadItem);
                String lenStr = connection.getHeaderField(HeaderField.CONTENT_LENGTH.getValue());
                //得到返回内容的长度
                long contentLength = Long.parseLong(TextUtils.isEmpty(lenStr) ? "-1" : lenStr);
                //文件总长度
                long totalLen;
                if (contentLength > 0) {
                    if (code == HttpURLConnection.HTTP_PARTIAL) {
                        String range = connection.getHeaderField(HeaderField.CONTENT_RANGE.getValue());
                        totalLen = Long.parseLong(range.substring(range.lastIndexOf("/") + 1));
                    } else {
                        totalLen = contentLength;
                    }
                    File file = new File(downloadItem.filePath);
                    if (file.exists()) {
                        if (file.length() == totalLen) {
                            respListener.onError(downloadItem.id, ErrorCode.EXIST, ErrorCode.EXIST.getMsg());
                            downloadItem.status = DownloadStatus.finish.getValue();
                            downloadItem.currLen = totalLen;
                            downloadItem.totalLen = totalLen;
                            downloadDao.updateRecord(downloadItem);
                            return;
                        }
                    } else {
                        if (!file.getParentFile().exists()) {
                            if (!file.getParentFile().mkdirs()) {
                                respListener.onError(downloadItem.id, ErrorCode.CREATE_DIR_FAILED,
                                        ErrorCode.CREATE_DIR_FAILED.getMsg());
                                failedStatus();
                                return;
                            }
                        }
                        if (!file.createNewFile()) {
                            respListener.onError(downloadItem.id, ErrorCode.CREATE_FILE_FAILED,
                                    ErrorCode.CREATE_FILE_FAILED.getMsg());
                            failedStatus();
                            return;
                        }
                    }
                    downloadItem.totalLen = totalLen;
                    downloadItem.status = DownloadStatus.downloading.getValue();
                    downloadDao.updateRecord(downloadItem);
                    respListener.onTotalLength(downloadItem.id, totalLen);

                    bis = new BufferedInputStream(connection.getInputStream());
                    fos = new FileOutputStream(file, true);

                    DecimalFormat df = new DecimalFormat("#.##");

                    byte[] buffer = new byte[1024];
                    //每秒多少k
                    double speed;
                    //单次更新进度的累计接受的长度
                    long receiveLen = 0L;
                    //下载累计长度
                    long getLen = 0L;
                    if (code == HttpURLConnection.HTTP_PARTIAL) {
                        getLen = file.length();
                    }
                    long startTime = System.currentTimeMillis();
                    int readLen;
                    while ((readLen = bis.read(buffer)) != -1) {
                        // 首次显示进度0%或上次下载的进度，以后每隔2% 更新一次进度
                        if (isFirstProgress.compareAndSet(false, true)) {
                            respListener.onProgress(downloadItem.id, (int) (getLen / (double) totalLen * 100),
                                    "0 K/s", getLen);
                        }
                        if (isPause()) {
                            pauseStatus();
                            return;
                        }
                        if (isCancel()) {
                            cancelStatus();
                            return;
                        }
                        getLen += readLen;
                        receiveLen += readLen;
                        fos.write(buffer, 0, readLen);
                        if (receiveLen / (double) totalLen * 100 >= 2 || getLen == totalLen) {
                            downloadItem.currLen = getLen;
                            if (getLen == totalLen) {
                                downloadItem.status = DownloadStatus.finish.getValue();
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA);
                                downloadItem.endTime = dateFormat.format(new Date());
                            }
                            downloadDao.updateRecord(downloadItem);

                            long useTime = System.currentTimeMillis() - startTime;
                            // 计算平均速度 毫秒
                            speed = ((double) receiveLen / 1024) / useTime;
                            speed = speed * 1000; // 每秒的速度K/s
                            String unit = " K/s";
                            if (speed >= 1024) {
                                // 如果大于1024KB 转MB
                               speed = speed / 1024;
                               unit = " M/s";
                            }
                            respListener.onProgress(downloadItem.id, (int) (getLen / (double) totalLen * 100),
                                    df.format(speed) + unit, getLen);
                            startTime = System.currentTimeMillis();
                            receiveLen = 0L;
                        }
                    }
                    respListener.onSuccess(downloadItem.id, downloadItem.filePath);
                } else {
                    respListener.onError(downloadItem.id, ErrorCode.INVALID_LENGTH, "ContentLength is " + contentLength);
                    failedStatus();
                }
            } else {
                respListener.onFailure(downloadItem.id, ErrorCode.RESP_ERROR, code, connection.getResponseMessage());
                failedStatus();
            }
        } catch (IOException e) {
            respListener.onError(downloadItem.id, ErrorCode.EXCEPTION, e.getMessage());
            failedStatus();
            Log.e(DownloadServiceImpl.class.getSimpleName(), Log.getStackTraceString(e));
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void failedStatus() {
        downloadItem.status = DownloadStatus.failed.getValue();
        downloadDao.updateRecord(downloadItem);
    }

    private synchronized void pauseStatus() {
        if (downloadItem.status != DownloadStatus.pause.getValue()) {
            respListener.onPause(downloadItem.id, downloadItem.filePath);
            downloadItem.status = DownloadStatus.pause.getValue();
            downloadDao.updateRecord(downloadItem);
        }
    }

    private synchronized void cancelStatus() {
        if (downloadDao.findRecordByIdFromCached(downloadItem.id) != null) {
            respListener.onCancel(downloadItem.id);
            if (isDelFile) {
                File file = new File(downloadItem.filePath);
                if (file.exists()) {
                    file.delete();
                }
            }
            downloadDao.delRecord(downloadItem.id);
        }
    }


    public void pause() {
        isPause.compareAndSet(false, true);
        pauseStatus();
    }

    public boolean isPause() {
        return isPause.get();
    }

    public void cancel(boolean isDelFile) {
        isCancel.compareAndSet(false, true);
        this.isDelFile = isDelFile;
        cancelStatus();
    }

    public boolean isCancel() {
        return isCancel.get();
    }

    public void resume() {
        isPause.compareAndSet(true, false);
    }

}
