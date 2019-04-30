package com.egbert.rconcise.enums;

/**
 * 下载/上传任务的状态码<br><br>
 * Created by Egbert on 3/18/2019.
 */
public enum TaskStatus {

    /**
     * 等待下载/上传
     */
    waiting(1),

    /**
     * 开始下载/上传
     */
    starting(2),

    /**
     * 正在下载/上传
     */
    running(3),

    /**
     * 暂停
     */
    pause(4),

    /**
     * 完成
     */
    finish(5),

    /**
     * 失败
     */
    failed(6);

    private int value;

    TaskStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
