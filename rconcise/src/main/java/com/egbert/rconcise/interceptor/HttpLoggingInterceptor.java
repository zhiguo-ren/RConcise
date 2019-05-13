package com.egbert.rconcise.interceptor;

import android.text.TextUtils;

import com.egbert.rconcise.internal.ContentType;
import com.egbert.rconcise.internal.HeaderField;
import com.egbert.rconcise.internal.NoBorderFormatStrategy;
import com.egbert.rconcise.internal.Utils;
import com.egbert.rconcise.internal.http.Request;
import com.egbert.rconcise.internal.http.Response;
import com.google.gson.Gson;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.egbert.rconcise.internal.Const.UTF8;
import static com.egbert.rconcise.internal.Utils.TAG;

/**
 * 请求响应日志拦截器
 * Created by Egbert on 3/6/2019.
 */
public class HttpLoggingInterceptor implements Interceptor {

    public enum Level {
        /** No logs. */
        NONE,

        /**
         * Logs request and response lines.
         *
         * <p>Example:
         * <pre>{@code
         * --> POST /greeting http/1.1 (3-byte body)
         *
         * <-- 200 OK (22ms, 6-byte body)
         * }</pre>
         */
        BASIC,

        /**
         * Logs request and response lines and their respective headers.
         *
         * <p>Example:
         * <pre>{@code
         * --> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         * <-- END HTTP
         * }</pre>
         */
        HEADERS,

        /**
         * Logs request and response lines and their respective headers and bodies (if present).
         *
         * <p>Example:
         * <pre>{@code
         * --> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         *
         * Hi?
         * --> END GET
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         *
         * Hello!
         * <-- END HTTP
         * }</pre>
         */
        BODY
    }

    private RLogger logger;

    public HttpLoggingInterceptor() {
        this(RLogger.DEFAULT);
        NoBorderFormatStrategy formatStrategy = NoBorderFormatStrategy.newBuilder()
                .showThreadInfo(false)    // 是否显示线程信息
                .tag(TAG)               // 全局Tag标签
                .build();
        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));
    }

    public HttpLoggingInterceptor(RLogger logger) {
        this.logger = logger;
    }

    private volatile Level level = Level.NONE;

    /** Change the level at which this interceptor logs. */
    public HttpLoggingInterceptor setLevel(Level level) {
        if (level == null) {
            throw new NullPointerException("level == null. Use Level.NONE instead.");
        }
        this.level = level;
        return this;
    }

    public Level getLevel() {
        return level;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Level level = this.level;

        Request request = chain.request();
        if (level == Level.NONE) {
            return chain.proceed(request);
        }

        boolean logBody = level == Level.BODY;
        boolean logHeaders = logBody || level == Level.HEADERS;

        Object reqParams = request.reqParams();
        boolean hasRequestBody = reqParams != null;

        String requestStartMessage = "--> " + request.method() + ' ' + request.url() + ' ' + "http/1.1";

        logger.log(requestStartMessage);

        if (logHeaders) {
            if (request.headers() != null) {
                for (Map.Entry<String, String> header : request.headers().entrySet()) {
                    logger.log(header.getKey()+ ": " + header.getValue());
                }
            }

            if (!logBody || !hasRequestBody) {
                logger.log("--> END " + request.method());
            } else {
                byte[] params = null;
                String contentType = null;
                if (request.headers() != null) {
                    contentType = request.headers().get(HeaderField.CONTENT_TYPE.getValue());
                }
                String type = ContentType.FORM_URLENCODED.getValue();
                if (TextUtils.isEmpty(contentType) || type.contains(contentType)) {
                    StringBuilder builder = Utils.parseParams(reqParams, false);
                    if (builder != null && builder.length() > 0) {
                        params = builder.toString().getBytes(UTF8);
                    }
                } else if (ContentType.JSON.getValue().contains(contentType)) {
                    String json = new Gson().toJson(reqParams);
                    params = json.getBytes(UTF8);
                } else {
                    params = reqParams.toString().getBytes(UTF8);
                }
                logger.log(new String(params, UTF8));
                logger.log("--> END " + request.method()
                        + " (" + (params == null ? 0 : params.length) + "-byte body)");
            }
        }

        long startNs = System.nanoTime();
        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            logger.log("<-- HTTP FAILED: " + e);
            throw e;
        }
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        String responseBody = response.respStr();
        long contentLength = TextUtils.isEmpty(responseBody) ? 0 : responseBody.getBytes().length;
        String bodySize = contentLength + "-byte";
        logger.log("<-- " + response.respCode() + ' ' + response.message() + ' '
                + response.request().url() + " (" + tookMs + "ms" + (!logHeaders ? ", "
                + bodySize + " body" : "") + ')');

        if (logHeaders) {
            Map<String, List<String>> headers = response.headers();
            for (String key : headers.keySet()) {
                List<String> info = headers.get(key);
                if (info != null && info.size() > 0) {
                    StringBuilder builder = new StringBuilder();
                    for (String value : info) {
                        builder.append(value);
                    }
                    if (key != null) {
                        logger.log(key + ": " + builder.toString());
                    }
                }
            }

            if (!logBody || TextUtils.isEmpty(responseBody)) {
                logger.log("<-- END HTTP");
            } else {
                logger.log(responseBody);
                logger.log("<-- END HTTP (" + responseBody.getBytes().length + "-byte body)");
            }
        }

        return response;
    }

    public interface RLogger {
        void log(String message);

        /**
         * 不知道日志处理类，就默认使用Logger
         */
        RLogger DEFAULT = new RLogger() {
            @Override
            public void log(String message) {
                Logger.d(message);
            }
        };
    }
}
