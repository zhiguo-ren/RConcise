package com.egbert.rconcise.enums;

/**
 * Created by Egbert on 3/18/2019.
 */
public enum TaskStopMode {

    /**
     * 自动停止下载任务
     */
    auto(0),

    /**
     * 手动停止下载任务
     */
    hand(1);

    TaskStopMode(Integer value) {
        this.value = value;
    }

    /**
     * 值
     */
    private Integer value;

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public static TaskStopMode getInst(int value) {
        for (TaskStopMode mode : TaskStopMode.values()) {
            if (mode.getValue() == value) {
                return mode;
            }
        }
        return TaskStopMode.auto;
    }
}
