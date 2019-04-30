package com.egbert.rconcise.download.listener;

import android.os.Handler;
import android.os.Looper;

import com.egbert.rconcise.internal.ErrorCode;

/**
 * Created by Egbert on 3/25/2019.
 */
public class DownloadListenerImpl implements IDownloadListener {
    private IDownloadObserver observer;
    private Handler handler = new Handler(Looper.getMainLooper());

    public DownloadListenerImpl(IDownloadObserver observer) {
        this.observer = observer;
    }

    @Override
    public void onSuccess(final int downloadId, final String filePath) {
        if (observer != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    observer.onSuccess(downloadId, filePath);
                }
            });
        }
    }

    @Override
    public void onCancel(final int downloadId) {
        if (observer != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    observer.onCancel(ErrorCode.CANCEL);
                }
            });
        }
    }

    @Override
    public void onPause(final int downloadId, String filePath) {
        if (observer != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    observer.onPause(downloadId);
                }
            });
        }
    }

    @Override
    public void onError(final int downloadId, final ErrorCode code, final String msg) {
        if (observer != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    observer.onError(downloadId, code, msg);
                }
            });
        }
    }

    @Override
    public void onFailure(final int downloadId, final ErrorCode code, final int httpCode, final String msg) {
        if (observer != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    observer.onFailure(downloadId, code, httpCode, msg);
                }
            });
        }
    }

    @Override
    public void onProgress(final int downloadId, final int percent, final String speed, final long bytes) {
        if (observer != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    observer.onProgress(downloadId, percent, speed, bytes);
                }
            });
        }
    }

    @Override
    public void onStart(final int downloadId, final long length) {
        if (observer != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    observer.onStart(downloadId, length);
                }
            });
        }
    }
}
