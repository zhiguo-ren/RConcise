package com.egbert.rconcise.download.listener;

import com.egbert.rconcise.internal.ErrorCode;

/**
 * 下载状态回调接口
 * Created by Egbert on 3/18/2019.
 */
public interface IDownloadObserver {

    /**
     * 下载开始 callback
     *
     * @param downloadId 下载id
     * @param totalLength 下载文件的总长度
     */
    void onStart(int downloadId, long totalLength);

    /**
     * 下载进度 callback
     *
     * @param downloadId 下载id
     * @param downloadPercent 下载的百分比
     * @param speed 下载速度
     * @param bytes 累计已下载字节数
     */
    void onProgress(int downloadId, int downloadPercent, String speed, long bytes);

    /**
     * 下载成功 callback
     */
    void onSuccess(int downloadId, String filePath);


    /**
     * 暂停下载 callback
     */
    void onPause(int downloadId);

    /**
     * 取消任务 callback
     */
    void onCancel(ErrorCode code);

    /**
     * 下载出错 callback
     *
     * @param downloadId 下载id
     * @param code 错误码
     * @param msg 错误信息
     */
    void onError(int downloadId, ErrorCode code, String msg);

    /**
     * 下载失败 callback
     * @param downloadId 下载id
     * @param code 错误码
     * @param msg 错误信息
     * @param httpCode http响应码
     */
    void onFailure(int downloadId, ErrorCode code, int httpCode, String msg);

}
