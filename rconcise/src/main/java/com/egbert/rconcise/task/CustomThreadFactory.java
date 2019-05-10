package com.egbert.rconcise.task;

import java.util.HashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Egbert on 5/8/2019.
 */
public class CustomThreadFactory implements ThreadFactory {
    public static final String NORMAL = "normal";
    public static final String DOWNLOAD = "download";
    public static final String UPLOAD = "upload";
    public static final HashMap<String, Thread> ROOT_RUNNABLES = new HashMap<>();
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    CustomThreadFactory(String namePrefix) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() :
                Thread.currentThread().getThreadGroup();
        this.namePrefix = namePrefix;
    }

    public void resetThreadNum() {
        threadNumber.set(1);
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r,
                namePrefix + "-pool" + "-thread-"
                        + threadNumber.get(), 0);
        if (t.isDaemon()) {
            t.setDaemon(false);
        }
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        if (threadNumber.getAndIncrement() == 1) {
            ROOT_RUNNABLES.put(namePrefix, t);
        }
        return t;
    }
}
