package com.egbert.rconcise.upload.listener;

import com.egbert.rconcise.internal.ErrorCode;

/**
 * 调用层使用的上传状态回调接口
 * Created by Egbert on 4/24/2019.
 */
public interface IUploadObserver {
    /**
     * 上传开始 callback
     * @param uploadId 上传任务记录Id
     * @param totalLen 上传任务总大小
     */
    void onStart(int uploadId, long totalLen);

    /**
     * 上传进度 callback
     *
     * @param uploadId 上传id
     * @param uploadPercent 上传的百分比
     * @param speed 上传速度
     * @param bytes 累计已上传字节数
     */
    void onProgress(int uploadId, int uploadPercent, String speed, long bytes);

    /**
     * 完成 callback
     */
    void onSuccess(int uploadId, String resp);

    /**
     * 暂停上传 callback
     */
    void onPause(int uploadId);

    /**
     * 取消任务 callback
     */
    void onCancel(ErrorCode code);

    /**
     * 上传出错callback
     *
     * @param uploadId 上传任务记录Id
     * @param code 错误码 参见：{@link ErrorCode}
     * @param msg 错误信息
     */
    void onError(int uploadId, ErrorCode code, String msg);

    /**
     * 下载失败 callback
     * @param uploadId 上传id
     * @param code 错误码 参见：{@link ErrorCode}
     * @param msg 错误信息
     * @param httpCode http响应码
     */
    void onFailure(int uploadId, ErrorCode code, int httpCode, String msg);
}
