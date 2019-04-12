package com.egbert.rconcise.download.enums;

/**
 * Created by Egbert on 3/18/2019.
 */
public enum Priority {
    /**
     * 手动下载的优先级
     */
    LOW(0),

    /**
     * 主动推送资源的手动恢复的优先级
     */
    MIDDLE(1),

    /**
     * 主动推送资源的优先级
     */
    HIGH(2);

    Priority(int value) {
        this.value = value;
    }

    private int value;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public static Priority getInst(int value) {
        for (Priority priority : Priority.values()) {
            if (priority.getValue() == value) {
                return priority;
            }
        }
        return Priority.MIDDLE;
    }
}
