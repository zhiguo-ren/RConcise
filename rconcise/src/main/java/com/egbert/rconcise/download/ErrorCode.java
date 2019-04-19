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
    CREATE_DIR_FAILED   (7, "创建文件目录失败"),
    INSER_DB_FAILED     (8, "插入数据库失败，请确认文件路径是否重复或下载任务是否重复添加"),
    CANCEL              (9, "下载任务已取消");

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
