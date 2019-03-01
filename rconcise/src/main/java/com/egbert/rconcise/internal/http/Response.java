package com.egbert.rconcise.internal.http;

import java.io.InputStream;
import java.util.HashMap;

/**
 * http 响应封装类
 * Created by Egbert on 3/1/2019.
 */
public final class Response {
    private final Request request;
    private final int code;
    private final String message;
    private final HashMap<String, String> headers;
    private final InputStream respIs;

    private Response(Builder builder) {
        this.request = builder.request;
        this.code = builder.code;
        this.message = builder.message;
        this.headers = builder.headers;
        this.respIs = builder.respIs;
    }

    public static class Builder {
        private Request request;
        private int code;
        private String message;
        private HashMap<String, String> headers;
        private InputStream respIs;


        public static Builder create() {
           return new Builder();
        }

        public Builder request(Request request) {
            this.request = request;
            return this;
        }

        public Builder code(int code) {
            this.code = code;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder headers(HashMap<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder respIs(InputStream respIs) {
            this.respIs = respIs;
            return this;
        }

        public Response build() {
            return new Response(this);
        }
    }
}
