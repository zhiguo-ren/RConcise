package com.egbert.rconcise.download;

import com.egbert.rconcise.database.annotation.Entity;
import com.egbert.rconcise.database.annotation.FieldName;
import com.egbert.rconcise.task.ReqTask;

/**
 * Created by Egbert on 3/15/2019.
 */
@Entity("download_info")
public class DownloadItem extends BaseItem<DownloadItem> {
    /**
     * 下载文件当前大小
     */
    @FieldName("curr_len")
    public Long currLen;
    /**
     * 下载文件总大小
     */
    @FieldName("total_len")
    public Long totalLen;
    /**
     * 下载文件显示名
     */
    @FieldName("file_name")
    public String fileName;
    /**
     * 下载存储的文件路径
     */
    @FieldName("file_path")
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
    @FieldName("start_time")
    public String startTime;
    /**
     * 下载结束时间
     */
    @FieldName("end_time")
    public String endTime;
    /**
     * 下载任务类型
     */
    @FieldName("user_id")
    public String userId;
    /**
     * 下载任务类型
     */
    @FieldName("task_type")
    public String taskType;
    /**
     * 下载优先级
     */
    public Integer priority;
    /**
     * 下载停止模式
     */
    @FieldName("stop_mode")
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
