package com.egbert.rconcise.internal.http;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.egbert.rconcise.RClient;
import com.egbert.rconcise.internal.ReqMethod;
import com.egbert.rconcise.listener.IHttpRespListener;
import com.egbert.rconcise.listener.IRespListener;
import com.egbert.rconcise.listener.JsonRespListenerImpl;
import com.egbert.rconcise.listener.StringRespListener;
import com.egbert.rconcise.task.ReqTask;
import com.egbert.rconcise.task.ThreadPoolManager;

import java.util.HashMap;
import java.util.Map;

/**
 * http 请求封装类
 * Created by Egbert on 3/1/2019.
 */
public final class Request {
    private final RClient rClient;
    private final IHttpRespListener respListener;
    private final Object reqParams;
    private final String url;
    private final HashMap<String, String> headerMap;
    private final String method;

    /**
     * method  请求方法必须全部大写
     * reqParams 请求参数的对象，可以为bean实体类和map对象，字段值统一用String
     * url  请求地址
     * header  请求头
     * respListener 响应结果监听接口
     * reqService 处理请求逻辑
     */
    private Request(Builder builder) {
        this.rClient = builder.rClient;
        this.respListener = builder.respListener;
        this.reqParams = builder.reqParams;
        this.url = builder.url;
        this.headerMap = builder.headerMap;
        this.method = builder.method;
    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    public RClient rClient() {
        return this.rClient;
    }

    public IHttpRespListener respListener() {
        return respListener;
    }

    public Object reqParams() {
        return reqParams;
    }

    public String url() {
        return url;
    }

    public HashMap<String, String> headers() {
        return headerMap;
    }

    public String method() {
        return method;
    }

    public static class Builder {
        private RClient rClient;
        private IHttpRespListener respListener;
        private Object reqParams;
        private String url;
        private HashMap<String, String> headerMap;
        private String method;
        private Map<String, String> params;

        private Builder(String url) {
            this.url = url;
        }

        public static Builder create(@NonNull String url) {
            return new Builder(url);
        }

        public Builder(Request req) {
            this.rClient = req.rClient;
            this.respListener = req.respListener;
            this.reqParams = req.reqParams;
            this.url = req.url;
            this.headerMap = req.headerMap;
            this.method = req.method;
        }

        public Builder client(@NonNull RClient rClient) {
            this.rClient = rClient;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        /**
         * 批量设置请求参数
         * @param reqParams 可接受 javabean实例 或 map对象, 但一次请求构建中只能选择一种
         */
        public Builder addParams(Object reqParams) {
            this.reqParams = reqParams;
            return this;
        }

        /**
         * 单个设置请求参数
         * @param name 参数名
         * @param value 参数值
         * @return
         */
        public Builder addParam(String name, String value) {
            if (params == null) {
                params = new HashMap<>();
            }
            params.put(name, value);
            return this;
        }

        /**
         * 设置响应结果监听器
         * @param resp json转实体对象的具体实体类型
         * @param listener 响应回调接口
         */
        public <T> Builder respListener(Class<T> resp, IRespListener<T> listener) {
            this.respListener = new JsonRespListenerImpl<>(resp, listener);
            return this;
        }

        /**
         * 设置响应结果监听器，该方法设置的回调接口返回String类型数据, 不做解析, 由调用者自行解析
         * @param listener 响应回调接口
         */
        public Builder respStrListener(IRespListener<String> listener) {
            this.respListener = new StringRespListener(listener);
            return this;
        }

        /**
         * 批量设置请求头属性
         */
        public Builder addHeaders(HashMap<String, String> headerMap) {
            if (headerMap != null) {
                if (this.headerMap == null) {
                    this.headerMap = headerMap;
                } else {
                    this.headerMap.putAll(headerMap);
                }
            }
            return this;
        }

        /**
         * 单个添加请求头属性
         */
        public Builder addHeader(String name, String value) {
            if (headerMap == null) {
                headerMap = new HashMap<>();
            }
            headerMap.put(name, value);
            return this;
        }

        /**
         * 发送请求
         * @param method 请求方法，不设置默认为GET
         */
        public Builder sendReq(String method) {
            if (TextUtils.isEmpty(method)) {
                this.method = ReqMethod.GET.getMethod();
            } else {
                this.method = method;
            }
            enqueue(build());
            return this;
        }

        public Builder get() {
            return sendReq(ReqMethod.GET.getMethod());
        }

        public Builder post() {
            return sendReq(ReqMethod.POST.getMethod());
        }

        public Request build() {
            if (this.rClient == null) {
                throw new NullPointerException("RClient cannot be null");
            }

            if (params != null) {
                if (reqParams instanceof Map) {
                    ((Map) reqParams).putAll(params);
                } else {
                    reqParams = params;
                }
            }
            return new Request(this);
        }

        private void enqueue(Request request) {
            try {
                ThreadPoolManager.getInst().execute(new ReqTask(request));
            } catch (InterruptedException e) {
                this.respListener.onError(e);
                e.printStackTrace();
            }
        }
    }

}
