package com.egbert.rconcise.upload;

import com.egbert.rconcise.database.annotation.Entity;
import com.egbert.rconcise.database.annotation.FieldName;
import com.egbert.rconcise.download.BaseItem;
import com.egbert.rconcise.task.ReqTask;

/**
 * Created by Egbert on 4/24/2019.
 */
@Entity("upload_info")
public class UploadItem extends BaseItem<UploadItem> {
    /**
     * 上传文件的当前字节数
     */
    @FieldName("curr_len")
    public Long currLen;
    /**
     * 上传文件总字节数
     */
    @FieldName("total_len")
    public Long totalLen;
    /**
     * 上传文件显示名
     */
    @FieldName("file_name")
    public String fileName;
    /**
     * 上传文件的本地路径
     */
    @FieldName("file_path")
    public String filePath;
    /**
     * 上传url
     */
    public String url;
    /**
     * 上传记录id
     */
    public Integer id;
    /**
     * 上传开始时间
     */
    @FieldName("start_time")
    public String startTime;
    /**
     * 上传结束时间
     */
    @FieldName("end_time")
    public String endTime;
    /**
     * 绑定当前的用户id
     */
    @FieldName("user_id")
    public String userId;
    /**
     * 上传任务类型
     */
    @FieldName("task_type")
    public String taskType;
    /**
     * 上传优先级
     * @see com.egbert.rconcise.enums.Priority Priority
     */
    public Integer priority;
    /**
     * 上传停止模式
     */
    @FieldName("stop_mode")
    public Integer stopMode;
    /**
     * 上传状态 对应{@code TaskStatus}的枚举类
     * @see com.egbert.rconcise.enums.TaskStatus TaskStatus
     */
    public Integer status;

    public transient ReqTask reqTask;

}
