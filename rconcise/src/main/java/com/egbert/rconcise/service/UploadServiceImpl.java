package com.egbert.rconcise.service;

import android.text.TextUtils;
import android.util.Log;

import com.egbert.rconcise.RClient;
import com.egbert.rconcise.RConcise;
import com.egbert.rconcise.enums.TaskStatus;
import com.egbert.rconcise.internal.Const;
import com.egbert.rconcise.internal.ContentType;
import com.egbert.rconcise.internal.ErrorCode;
import com.egbert.rconcise.internal.HeaderField;
import com.egbert.rconcise.internal.ReqMethod;
import com.egbert.rconcise.internal.Utils;
import com.egbert.rconcise.internal.http.IRequest;
import com.egbert.rconcise.upload.MultiPartBody;
import com.egbert.rconcise.upload.RUpload;
import com.egbert.rconcise.upload.RUploadManager;
import com.egbert.rconcise.upload.UploadDao;
import com.egbert.rconcise.upload.UploadItem;
import com.egbert.rconcise.upload.listener.IUploadListener;
import com.egbert.rconcise.upload.listener.UploadListenerImpl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.HttpsURLConnection;

import static com.egbert.rconcise.internal.Const.BOUNDARY;
import static com.egbert.rconcise.internal.Const.BOUNDARY_PREFIX;
import static com.egbert.rconcise.internal.Const.CRLF;
import static com.egbert.rconcise.internal.Const.UTF8;

/**
 * Created by Egbert on 3/19/2019.
 */
public class UploadServiceImpl implements IReqService, IDownloadOrUploadReqService {
    private RUpload rUpload;
    private UploadItem uploadItem;
    private UploadDao uploadDao;
    private IUploadListener respListener;
    private AtomicBoolean isPause = new AtomicBoolean(false);
    private AtomicBoolean isCancel = new AtomicBoolean(false);
    private AtomicBoolean isFirstProgress = new AtomicBoolean(false);
    private DecimalFormat df = new DecimalFormat("#.##");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA);

    @Override
    public void setRequest(IRequest request) {
        rUpload = (RUpload) request;
        respListener = new UploadListenerImpl(rUpload.observer());
        uploadDao = RUploadManager.inst().getUploadDao();
    }

    public void setUploadItem(UploadItem item) {
        this.uploadItem = item;
    }

    private long[] calcContentLength(long start, long middle, long end, ArrayList<MultiPartBody.Part> parts)
            throws UnsupportedEncodingException {
        long currTotalLen = 0;
        long totalLen = 0;
        for (int i = 0; i < parts.size(); i++) {
            if (i == 0) {
                currTotalLen += start;
                totalLen += start;
            } else {
                currTotalLen += middle;
                totalLen += middle;
            }
            MultiPartBody.Part part = parts.get(i);
            long header = part.getPartHeaders().getBytes(UTF8).length;
            currTotalLen += header;
            totalLen += header;
            if (part.isFile()) {
                totalLen += ((File) part.getContent()).length();
                currTotalLen += ((File) part.getContent()).length() - part.getBeginIndex();
            } else {
                long content = ((String) part.getContent()).getBytes(UTF8).length;
                currTotalLen += content;
                totalLen += content;
            }
        }
        currTotalLen += end;
        totalLen += end;
        return new long[] {currTotalLen, totalLen};
    }

    @Override
    public void execute() {
        HttpURLConnection connection = null;
        try {
            if (isPause() || isCancel()) {
                return;
            }
            URL reqUrl = new URL(rUpload.url());
            connection = (HttpURLConnection) reqUrl.openConnection();
            RClient rClient = RConcise.inst().rClient(rUpload.rClientKey());
            if (reqUrl.getProtocol().equalsIgnoreCase(Const.HTTPS) && rClient.isSelfCert()) {
                ((HttpsURLConnection)connection).setSSLSocketFactory(rClient.getSSlSocketFactory());
            }
            connection.setRequestMethod(ReqMethod.POST.getMethod());
            connection.setConnectTimeout(20000);
            connection.setDoOutput(true);

            byte[] startBoundary = (BOUNDARY_PREFIX + BOUNDARY + CRLF).getBytes(UTF8);
            byte[] middleBoundary = (CRLF + BOUNDARY_PREFIX + BOUNDARY + CRLF).getBytes(UTF8);
            byte[] endLine = (CRLF + BOUNDARY_PREFIX + BOUNDARY + BOUNDARY_PREFIX + CRLF).getBytes(UTF8);
            ArrayList<MultiPartBody.Part> parts = rUpload.multiPartBody().getBodyParts();
            long[] totalLen = calcContentLength(startBoundary.length, middleBoundary.length,
                    endLine.length, parts);
            HashMap<String, String> headers = rUpload.headers();
            if (headers == null) {
                headers = RUploadManager.getsHeaders();
            } else {
                headers.putAll(RUploadManager.getsHeaders());
            }
            headers.put(HeaderField.CONNECTION.getValue(), "Keep-Alive");
            headers.put(HeaderField.CONTENT_TYPE.getValue(), ContentType.MULTIPART.getValue()
                    + " boundary=" + BOUNDARY);
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (!TextUtils.isEmpty(entry.getValue())) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            uploadItem.totalLen = totalLen[1];
            uploadItem.status = TaskStatus.starting.getValue();
            uploadDao.updateRecord(uploadItem);
            respListener.onStart(uploadItem.id, totalLen[1]);

            //单次更新进度的累计上传的长度
            long writeLen = 0L;
            //累计上传长度
            long uploadLen = 0L;

            long startTime = System.currentTimeMillis();

            BufferedOutputStream writer = new BufferedOutputStream(connection.getOutputStream());
            // 写part
            for (int i = 0; i < parts.size(); i++) {
                if (isPause()) {
                    writer.close();
                    pauseStatus();
                    return;
                }
                if (isCancel()) {
                    writer.close();
                    cancelStatus();
                    return;
                }
                if (i == 0) {
                    writer.write(startBoundary);
                    writeLen += startBoundary.length;
                    uploadLen += startBoundary.length;
                } else {
                    writer.write(middleBoundary);
                    writeLen += middleBoundary.length;
                    uploadLen += middleBoundary.length;
                }
                MultiPartBody.Part part = parts.get(i);
                long beginIndex = part.getBeginIndex();
                if (beginIndex > 0) {
                    uploadLen += beginIndex;
                }
                // 首次显示进度0%或上次上传的进度，以后每隔2% 更新一次进度
                if (isFirstProgress.compareAndSet(false, true)) {
                    respListener.onProgress(uploadItem.id, (int) (uploadLen / (double) totalLen[1] * 100),
                            "0 K/s", uploadLen);
                }
                byte[] partHeader = part.getPartHeaders().getBytes(UTF8);
                writer.write(partHeader);
                writeLen += partHeader.length;
                uploadLen += partHeader.length;
                if (part.isFile()) {
                    File file = ((File) part.getContent());
                    if (!file.exists()) {
                        respListener.onError(uploadItem.id, ErrorCode.FILE_NOT_EXIST,
                                ErrorCode.FILE_NOT_EXIST.getMsg());
                        failedStatus();
                        writer.close();
                        return;
                    }
                    int len;
                    byte[] bytes = new byte[4096];
                    FileInputStream fis = new FileInputStream(file);
                    if (beginIndex > 0) {
                        fis.skip(beginIndex);
                    }
                    while ((len = fis.read(bytes)) != -1) {
                        if (isPause()) {
                            fis.close();
                            writer.close();
                            pauseStatus();
                            return;
                        }
                        if (isCancel()) {
                            fis.close();
                            writer.close();
                            cancelStatus();
                            return;
                        }
                        writer.write(bytes, 0, len);
                        writeLen += len;
                        uploadLen += len;
                        if (writeLen / (double) totalLen[1] * 100 >= 2 || uploadLen == totalLen[1] ) {
                            progress(writeLen, totalLen[1], uploadLen, startTime);
                            writeLen = 0;
                            startTime = System.currentTimeMillis();
                        }
                    }
                    fis.close();
                } else {
                    long content = ((String) part.getContent()).getBytes(UTF8).length;
                    writer.write(((String) part.getContent()).getBytes(UTF8));
                    uploadLen += content;
                    writeLen += content;
                }
            }
            writer.write(endLine);
            uploadLen += endLine.length;
            /*if (writeLen / (double) totalLen[1] * 100 >= 2 || uploadLen == totalLen[1]) {
                progress(writeLen, totalLen[1], uploadLen, startTime);
            }*/
            writer.close();

            // 响应码
            int code = connection.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK) {
                if (uploadLen == totalLen[1] ) {
                    respListener.onProgress(uploadItem.id, (int) (uploadLen / (double) totalLen[1] * 100),
                            "0 K/s", uploadLen);
                }
                uploadItem.currLen = totalLen[1];
                uploadItem.status = TaskStatus.finish.getValue();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA);
                uploadItem.endTime = dateFormat.format(new Date());
                uploadDao.updateRecord(uploadItem);
                respListener.onSuccess(uploadItem.id, Utils.handleInputStream(connection.getInputStream()));
            } else {
                respListener.onFailure(uploadItem.id, ErrorCode.RESP_ERROR, code, connection.getResponseMessage());
                failedStatus();
            }
        } catch (Exception e) {
            respListener.onError(uploadItem.id, ErrorCode.EXCEPTION, e.getMessage());
            failedStatus();
            Log.e(UploadServiceImpl.class.getSimpleName(), Log.getStackTraceString(e));
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void progress(long writeLen, long totalLen, long uploadLen, long startTime) {
        uploadItem.currLen = uploadLen;
        if (uploadLen == totalLen) {
            uploadItem.status = TaskStatus.finish.getValue();
            uploadItem.endTime = dateFormat.format(new Date());
        }
        uploadDao.updateRecord(uploadItem);

        long useTime = System.currentTimeMillis() - startTime;
        // 计算平均速度 毫秒
        double speed = ((double) writeLen / 1024) / useTime;
        speed = speed * 1000; // 每秒的速度K/s
        String unit = " K/s";
        if (speed >= 1024) {
            // 如果大于1024KB 转MB
            speed = speed / 1024;
            unit = " M/s";
        }
        respListener.onProgress(uploadItem.id, (int) (uploadLen / (double) totalLen * 100),
                df.format(speed) + unit, uploadLen);
    }

    private void failedStatus() {
        uploadItem.status = TaskStatus.failed.getValue();
        uploadDao.updateRecord(uploadItem);
    }

    private synchronized void pauseStatus() {
        if (uploadItem.status != TaskStatus.pause.getValue()) {
            respListener.onPause(uploadItem.id);
            uploadItem.status = TaskStatus.pause.getValue();
            uploadDao.updateRecord(uploadItem);
        }
    }

    private synchronized void cancelStatus() {
        if (uploadDao.findRecordByIdFromCached(uploadItem.id) != null) {
            respListener.onCancel(ErrorCode.CANCEL);
            uploadDao.delRecord(uploadItem.id);
        }
    }


    @Override
    public void pause() {
        isPause.compareAndSet(false, true);
        pauseStatus();
    }

    public boolean isPause() {
        return isPause.get();
    }

    @Override
    public void cancel(boolean flag) {
        isCancel.compareAndSet(false, true);
        cancelStatus();
    }

    public boolean isCancel() {
        return isCancel.get();
    }

    @Override
    public void resume() {
        isPause.compareAndSet(true, false);
    }

}
