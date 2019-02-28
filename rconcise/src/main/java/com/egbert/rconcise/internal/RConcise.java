package com.egbert.rconcise.internal;

import android.support.annotation.NonNull;

import com.egbert.rconcise.listener.IHttpRespListener;
import com.egbert.rconcise.listener.IRespListener;
import com.egbert.rconcise.listener.JsonRespListenerImpl;
import com.egbert.rconcise.listener.StringRespListener;
import com.egbert.rconcise.service.IReqService;
import com.egbert.rconcise.service.ReqServiceImpl;
import com.egbert.rconcise.task.ReqTask;
import com.egbert.rconcise.task.ThreadPoolManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Egbert on 2/25/2019.
 */
public class RConcise {
    /**
     * baseUrl 为请求地址的公共前半部分，后边追加具体接口的路径，只需在app中设置一次，必须以'/'结尾；
     */
    private static String sBaseUrl;
    private final IReqService reqService;
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
    private RConcise(Builder builder) {
        this.reqService = builder.reqService;
        this.respListener = builder.respListener;
        this.reqParams = builder.reqParams;
        this.url = builder.url;
        this.headerMap = builder.headerMap;
        this.method = builder.method;
    }

    public static void setBaseUrl(String baseUrl) {
        if (Utils.verifyUrl(baseUrl, true)) {
            sBaseUrl = baseUrl;
        }
    }

    public static String getBaseUrl() {
        return sBaseUrl;
    }

    public IReqService getReqService() {
        return reqService;
    }

    public IHttpRespListener getRespListener() {
        return respListener;
    }

    public Object getReqParams() {
        return reqParams;
    }

    public String getUrl() {
        return url;
    }

    public HashMap<String, String> getHeaderMap() {
        return headerMap;
    }

    public String getMethod() {
        return method;
    }

    public static class Builder {
        private IReqService reqService;
        private IHttpRespListener respListener;
        private Object reqParams;
        private final String url;
        private HashMap<String, String> headerMap;
        private String method;
        private Map<String, String> params;

        private Builder(String url) {
            this.url = url;
        }

        public static Builder create(@NonNull String url) {
            return new Builder(url);
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
            this.reqService = new ReqServiceImpl();
            this.respListener = new JsonRespListenerImpl<>(resp, listener);
            return this;
        }

        /**
         * 设置响应结果监听器，该方法设置的回调接口返回String类型数据, 不做解析, 由调用者自行解析
         * @param listener 响应回调接口
         */
        public Builder respStrListener(IRespListener<String> listener) {
            this.reqService = new ReqServiceImpl();
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
         * 单个设置请求头属性
         */
        public Builder addHeader(String name, String value) {
            if (headerMap == null) {
                headerMap = new HashMap<>();
            }
            headerMap.put(name, value);
            return this;
        }

        public void sendReq(@NonNull String method) {
            this.method = method;
            build();
        }

        public void get() {
            sendReq(ReqMethod.GET.getMethod());
        }

        public void post() {
            sendReq(ReqMethod.POST.getMethod());
        }

        private void build() {
            if (params != null) {
                if (reqParams instanceof Map) {
                    ((Map) reqParams).putAll(params);
                } else {
                    reqParams = params;
                }
            }
            try {
                ThreadPoolManager.getInst().execute(new ReqTask(new RConcise(this)));
            } catch (InterruptedException e) {
                this.respListener.onError(e);
                e.printStackTrace();
            }
        }
    }

}
