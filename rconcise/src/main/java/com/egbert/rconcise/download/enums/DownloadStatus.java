package com.egbert.rconcise.download.enums;

/**
 * Created by Egbert on 3/18/2019.
 */
public enum DownloadStatus {

    /**
     * 等待下载
     */
    waiting(0),

    /**
     * 开始下载
     */
    starting(1),

    /**
     * 下载中
     */
    downloading(2),

    /**
     * 暂停
     */
    pause(3),

    /**
     * 完成
     */
    finish(4),

    /**
     * 失败
     */
    failed(5);

    private int value;

    DownloadStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
