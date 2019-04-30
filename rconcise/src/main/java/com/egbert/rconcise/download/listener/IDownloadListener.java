package com.egbert.rconcise.download.listener;

import com.egbert.rconcise.internal.ErrorCode;

/**
 * Created by Egbert on 3/18/2019.
 */
public interface IDownloadListener {

    void onSuccess(int downloadId, String filePath);

    void onCancel(int downloadId);

    void onPause(int downloadId, String filePath);

    void onError(int downloadId, ErrorCode code, String msg);

    void onFailure(int downloadId, ErrorCode code, int httpCode, String msg);

    void onStart(int downloadId, long length);

    void onProgress(int downloadId, int percent, String speed, long bytes);
}
