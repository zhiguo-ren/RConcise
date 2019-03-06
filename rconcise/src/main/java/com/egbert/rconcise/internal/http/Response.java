package com.egbert.rconcise.internal.http;

import java.util.List;
import java.util.Map;

/**
 * http 响应封装类
 * Created by Egbert on 3/1/2019.
 */
public final class Response {
    private final Request request;
    private final int respCode;
    private final String message;
    private final Map<String, List<String>> headers;
    private final long reqStartTime;
    private final long respEndTime;
    private final String respStr;

    private Response(Builder builder) {
        this.request = builder.request;
        this.respCode = builder.code;
        this.message = builder.message;
        this.headers = builder.headers;
        this.reqStartTime = builder.reqStartTime;
        this.respEndTime = builder.respEndTime;
        this.respStr = builder.respStr;
    }

    public Request request() {
        return request;
    }

    public int respCode() {
        return respCode;
    }

    public String respStr() {
        return respStr;
    }

    public String message() {
        return message;
    }

    public long startTime() {
        return reqStartTime;
    }

    public long endTime() {
        return respEndTime;
    }

    public Map<String, List<String>> headers() {
        return headers;
    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    public static class Builder {
        private Request request;
        private int code;
        private String message;
        private Map<String, List<String>> headers;
        private long reqStartTime;
        private long respEndTime;
        private String respStr;

        public static Builder create() {
           return new Builder();
        }

        private Builder() {
        }

        public Builder(Response resp) {
            this.request = resp.request;
            this.code = resp.respCode;
            this.message = resp.message;
            this.headers = resp.headers;
            this.respStr = resp.respStr;
        }

        public Builder request(Request request) {
            this.request = request;
            return this;
        }

        public Builder code(int code) {
            this.code = code;
            return this;
        }

        public Builder respStr(String respStr) {
            this.respStr = respStr;
            return this;
        }

        public Builder reqStartTime(long time) {
            this.reqStartTime = time;
            return this;
        }

        public Builder respEndTime(long time) {
            this.respEndTime = time;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder headers(Map<String, List<String>> headers) {
            this.headers = headers;
            return this;
        }

        public Response build() {
            return new Response(this);
        }
    }
}
