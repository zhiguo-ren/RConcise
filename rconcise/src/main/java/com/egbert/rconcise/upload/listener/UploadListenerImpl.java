package com.egbert.rconcise.upload.listener;

import android.os.Handler;
import android.os.Looper;

import com.egbert.rconcise.internal.ErrorCode;

/**
 * Created by Egbert on 4/25/2019.
 */
public class UploadListenerImpl implements IUploadListener {
    private IUploadObserver observer;
    private Handler handler = new Handler(Looper.getMainLooper());

    public UploadListenerImpl(IUploadObserver observer) {
        this.observer = observer;
    }

    @Override
    public void onStart(final int uploadId, final long totalLen) {
        if (observer != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    observer.onStart(uploadId, totalLen);
                }
            });
        }
    }

    @Override
    public void onProgress(final int uploadId, final int uploadPercent, final String speed, final long bytes) {
        if (observer != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    observer.onProgress(uploadId, uploadPercent, speed, bytes);
                }
            });
        }
    }

    @Override
    public void onSuccess(final int uploadId, final String resp) {
        if (observer != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    observer.onSuccess(uploadId, resp);
                }
            });
        }
    }

    @Override
    public void onPause(final int uploadId) {
        if (observer != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    observer.onPause(uploadId);
                }
            });
        }
    }

    @Override
    public void onCancel(final ErrorCode code) {
        if (observer != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    observer.onCancel(code);
                }
            });
        }
    }

    @Override
    public void onError(final int uploadId, final ErrorCode code, final String msg) {
        if (observer != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    observer.onError(uploadId, code, msg);
                }
            });
        }
    }

    @Override
    public void onFailure(final int uploadId, final ErrorCode code, final int httpCode, final String msg) {
        if (observer != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    observer.onFailure(uploadId, code, httpCode, msg);
                }
            });
        }
    }
}
