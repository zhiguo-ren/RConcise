package com.egbert.rconcise.internal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.LogStrategy;
import com.orhanobut.logger.LogcatLogStrategy;

import static com.egbert.rconcise.internal.Utils.checkNotNull;

/**
 * 自定义logger日志边框（取消上下边框）
 * Created by Egbert on 3/8/2019.
 */
public class NoBorderFormatStrategy implements FormatStrategy {
    private static final char HORIZONTAL_LINE = '│';
    /**
     * Android's max limit for a log entry is ~4076 bytes,
     * so 4000 bytes is used as chunk size since default charset
     * is UTF-8
     */
    private static final int CHUNK_SIZE = 4000;

    private final boolean showThreadInfo;
    @NonNull private final LogStrategy logStrategy;
    @Nullable private final String tag;

    private NoBorderFormatStrategy(@NonNull NoBorderFormatStrategy.Builder builder) {
        checkNotNull(builder);

        showThreadInfo = builder.showThreadInfo;
        logStrategy = builder.logStrategy;
        tag = builder.tag;
    }

    @NonNull public static NoBorderFormatStrategy.Builder newBuilder() {
        return new NoBorderFormatStrategy.Builder();
    }

    @Override
    public void log(int priority, @Nullable String onceOnlyTag, @NonNull String message) {
        checkNotNull(message);

        String tag = formatTag(onceOnlyTag);
        logHeaderContent(priority, tag);
        //get bytes of message with system's default charset (which is UTF-8 for Android)
        byte[] bytes = message.getBytes();
        int length = bytes.length;
        if (length <= CHUNK_SIZE) {
            logContent(priority, tag, message);
            return;
        }
        for (int i = 0; i < length; i += CHUNK_SIZE) {
            int count = Math.min(length - i, CHUNK_SIZE);
            //create a new String with system's default charset (which is UTF-8 for Android)
            logContent(priority, tag, new String(bytes, i, count));
        }
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    private void logHeaderContent(int logType, @Nullable String tag) {
//        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        if (showThreadInfo) {
            logChunk(logType, tag, HORIZONTAL_LINE + " Thread: " + Thread.currentThread().getName());
        }
    }

    private void logContent(int logType, @Nullable String tag, @NonNull String chunk) {
        checkNotNull(chunk);

        String[] lines = chunk.split(System.getProperty("line.separator"));
        for (String line : lines) {
            logChunk(logType, tag, HORIZONTAL_LINE + " " + line);
        }
    }

    private void logChunk(int priority, @Nullable String tag, @NonNull String chunk) {
        checkNotNull(chunk);

        logStrategy.log(priority, tag, chunk);
    }

    @Nullable private String formatTag(@Nullable String tag) {
        if (!TextUtils.isEmpty(tag) && !this.tag.equals(tag)) {
            return this.tag + "-" + tag;
        }
        return this.tag;
    }

    public static class Builder {
        boolean showThreadInfo = true;
        @Nullable LogStrategy logStrategy;
        @Nullable String tag = "PRETTY_LOGGER";

        private Builder() {
        }

        @NonNull public NoBorderFormatStrategy.Builder showThreadInfo(boolean val) {
            showThreadInfo = val;
            return this;
        }

        @NonNull public NoBorderFormatStrategy.Builder logStrategy(@Nullable LogStrategy val) {
            logStrategy = val;
            return this;
        }

        @NonNull public NoBorderFormatStrategy.Builder tag(@Nullable String tag) {
            this.tag = tag;
            return this;
        }

        @NonNull public NoBorderFormatStrategy build() {
            if (logStrategy == null) {
                logStrategy = new LogcatLogStrategy();
            }
            return new NoBorderFormatStrategy(this);
        }
    }
}
