package com.egbert.rconcise.download;

import com.egbert.rconcise.database.annotation.Entity;
import com.egbert.rconcise.task.ReqTask;

/**
 * Created by Egbert on 3/15/2019.
 */
@Entity("download_info")
public class DownloadItem extends BaseItem<DownloadItem> {
    /**
     * 下载文件当前大小
     */
    public long currLen;
    /**
     * 下载文件总大小
     */
    public long totalLen;
    /**
     * 下载文件显示名
     */
    public String fileName;
    /**
     * 下载存储的文件路径
     */
    public String filePath;
    /**
     * 下载url
     */
    public String url;
    /**
     * 下载记录id
     */
    public Integer id;
    /**
     * 下载开始时间
     */
    public String startTime;
    /**
     * 下载结束时间
     */
    public String endTime;
    /**
     * 下载任务类型
     */
    public String userId;
    /**
     * 下载任务类型
     */
    public String taskType;
    /**
     * 下载优先级
     */
    public String priority;
    /**
     * 下载停止模式
     */
    public Integer stopMode;
    /**
     * 下载的状态
     */
    public Integer status;

    public transient ReqTask reqTask;

    public DownloadItem() {
    }

    public DownloadItem(String filePath, String url) {
        this.filePath = filePath;
        this.url = url;
    }
}
