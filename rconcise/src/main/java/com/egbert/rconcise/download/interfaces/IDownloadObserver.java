package com.egbert.rconcise.download.interfaces;

import com.egbert.rconcise.download.ErrorCode;

/**
 * 断点续传监听接口
 * Created by Egbert on 3/18/2019.
 */
public interface IDownloadObserver {

    /**
     * 暂停下载
     */
    void onPause(int downloadId);

    /**
     * 获取下载文件总长度
     *
     * @param downloadId 下载id
     * @param totalLength 下载文件总的长度
     */
    void onTotalLength(int downloadId, long totalLength);

    /**
     * 下载进度
     *
     * @param downloadId 下载id
     * @param downloadPercent 下载的百分比
     * @param speed 下载速度
     */
    void onCurrProgress(int downloadId, double downloadPercent, long speed);

    /**
     * 下载成功
     */
    void onSuccess(int downloadId, String filePath);

    /**
     * 下载失败监听
     *
     * @param downloadId 下载id
     * @param code 错误码
     * @param msg 错误信息
     */
    void onError(int downloadId, ErrorCode code, String msg);

    /**
     * @param downloadId 下载id
     * @param code 错误码
     * @param msg 错误信息
     * @param httpCode http响应码
     */
    void onFailure(int downloadId, ErrorCode code, int httpCode, String msg);

}
