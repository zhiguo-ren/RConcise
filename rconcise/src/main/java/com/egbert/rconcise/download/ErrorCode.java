package com.egbert.rconcise.download;

/**
 * 状态码常量类
 * Created by Egbert on 3/19/2019.
 */
public enum ErrorCode {

    /**
     * 已下载
     */
    EXIST               (1, "文件已下载"),
    RESP_ERROR          (2, "http响应码非200/206"),
    DOWNLOADING         (3, "下载任务已存在，请勿重复添加"),
    EXCEPTION           (4, "捕获到异常"),
    INVALID_LENGTH      (5, "ContentLength <= 0"),
    CREATE_FILE_FAILED  (6, "创建文件失败"),
    USER_CANCEL         (7, "用户已取消/暂停任务");

    private int code;
    private String msg;

    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
