package com.egbert.rconcise.enums;

/**
 * 任务优先级枚举
 * Created by Egbert on 3/18/2019.
 */
public enum Priority {
    /**
     * 底
     */
    LOW(1),

    /**
     * 中
     */
    MIDDLE(2),

    /**
     * 高
     */
    HIGH(3);

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
