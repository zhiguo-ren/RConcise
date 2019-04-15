package com.egbert.rconcise.service;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.egbert.rconcise.download.DownloadDao;
import com.egbert.rconcise.download.DownloadItem;
import com.egbert.rconcise.download.ErrorCode;
import com.egbert.rconcise.download.RDownload;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.egbert.rconcise.download.RDownloadManager.DEF_PATH;

/**
 * Created by Egbert on 3/19/2019.
 */
public class DownloadServiceImpl implements IReqService {
    private RDownload rDownload;
    private DownloadItem downloadItem;
    private DownloadDao downloadDao;
    private IDownloadListener respListener;
    private String packageName;
    private AtomicBoolean isPause = new AtomicBoolean(false);

    @Override
    public void setRequest(IRequest request) {
        rDownload = (RDownload) request;
        respListener = new DownloadListenerImpl(rDownload.observer());
    }

    public void setDownloadItem(DownloadItem item) {
        this.downloadItem = item;
    }

    public void setDownloadDao(DownloadDao downloadDao) {
        this.downloadDao = downloadDao;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public void execute() {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(rDownload.url()).openConnection();
            String method = rDownload.method().toUpperCase();
            boolean isPost = ReqMethod.POST.getMethod().equals(method);
            connection.setRequestMethod(method);
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
            connection.connect();
            if (isPost && params != null) {
                BufferedOutputStream writer = new BufferedOutputStream(connection.getOutputStream());
                writer.write(params);
                writer.close();
            }
            int code = connection.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK || code == HttpURLConnection.HTTP_PARTIAL) {
                String lenStr = connection.getHeaderField(HeaderField.CONTENT_LENGTH.getValue());
                //得到下载的长度
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
                        }
                    } else {
                        if (!file.getParentFile().exists()) {
                            file.mkdirs();
                        }
                        if (!file.createNewFile()) {
                            respListener.onError(downloadItem.id, ErrorCode.CREATE_FILE_FAILED,
                                    ErrorCode.CREATE_FILE_FAILED.getMsg());
                        } else {
                            downloadItem.totalLen = totalLen;
                            downloadItem.status = DownloadStatus.downloading.getValue();
                            downloadDao.updateRecord(downloadItem);
                            respListener.onTotalLength(downloadItem.id, totalLen);
                            long startTime = System.currentTimeMillis();
                            //用于计算每秒多少k
                            long speed = 0L;
                            //花费时间
                            long useTime = 0L;
                            //下载的长度
                            long getLen = 0L;
                            //接受的长度
                            long receiveLen = 0L;
                            boolean bufferLen = false;
                            //单位时间下载的字节数
                            long calcSpeedLen = 0L;
                            BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
                            FileOutputStream fos = new FileOutputStream(file, true);
                            BufferedOutputStream bos = new BufferedOutputStream(fos);
                            int len = 1;
                            byte[] buffer = new byte[1024];
                            while ((len = bis.read(buffer)) != -1) {
                                if (isPause()) {
                                    respListener.onError(downloadItem.id, ErrorCode.USER_CANCEL,
                                            ErrorCode.USER_CANCEL.getMsg());
                                    return;
                                }
                                bos.write(buffer, 0, len);

                            }

                            respListener.onSuccess(downloadItem.id, downloadItem.filePath);
                        }
                    }
                } else {
                    respListener.onError(downloadItem.id, ErrorCode.INVALID_LENGTH, "ContentLength is " + length);
                }
            } else {
                respListener.onFailure(downloadItem.id, ErrorCode.RESP_ERROR, code, connection.getResponseMessage());
            }
        } catch (IOException e) {
            respListener.onError(downloadItem.id, ErrorCode.EXCEPTION, e.getMessage());
            Log.e(DownloadServiceImpl.class.getSimpleName(), Log.getStackTraceString(e));
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public void createDownloadItem() {
        StringBuilder path = new StringBuilder();
        if (!TextUtils.isEmpty(rDownload.directory())) {
            path.append(rDownload.directory());
        } else {
            path.append(Environment.getExternalStorageDirectory().getAbsolutePath())
                    .append(File.separator)
                    .append(packageName)
                    .append(File.separator)
                    .append(DEF_PATH)
                    .append(File.separator);
        }
        if (!TextUtils.isEmpty(rDownload.fileName())) {
            path.append(rDownload.fileName());
        }
        DownloadItem item = new DownloadItem();
        item.filePath = path.toString();
        item.fileName = rDownload.fileName();
        item.url = rDownload.url();
    }

    public void pause() {
        isPause.compareAndSet(false, true);
    }

    public boolean isPause() {
        return isPause.get();
    }
}
