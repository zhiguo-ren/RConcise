package com.egbert.rconcise.internal.http;

import android.app.Activity;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.egbert.rconcise.RClient;
import com.egbert.rconcise.internal.ReqMethod;
import com.egbert.rconcise.listener.IHttpHeaderListener;
import com.egbert.rconcise.listener.IHttpRespListener;
import com.egbert.rconcise.listener.IRespListener;
import com.egbert.rconcise.listener.JsonRespListenerImpl;
import com.egbert.rconcise.listener.StringRespListener;
import com.egbert.rconcise.task.ReqTask;
import com.egbert.rconcise.task.ThreadPoolManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * http 请求封装类,通过该类封装请求信息<p>
 * Created by Egbert on 3/1/2019.
 */
public final class Request implements IRequest {
    private final RClient rClient;
    private final IHttpRespListener respListener;
    private final IHttpHeaderListener headerListener;
    private final Object reqParams;
    private final String url;
    private final HashMap<String, String> headerMap;
    private final String method;
    private final HashMap<String, String> pathMap;
    private final ArrayList<String> pathList;
    private final Activity activity;
    private final boolean isInBody;

    /**
     * method  请求方法必须全部大写<p>
     * reqParams 请求参数的对象，可以为bean实体类和map对象，字段值统一用String<p>
     * url  请求地址<p>
     * header  请求头<p>
     * respListener 响应结果监听接口<p>
     * reqService 处理请求逻辑<p>
     */
    private Request(Builder builder) {
        this.rClient = builder.rClient;
        this.respListener = builder.respListener;
        this.headerListener = builder.headerListener;
        this.reqParams = builder.reqParams;
        this.url = builder.url;
        this.headerMap = builder.headerMap;
        this.method = builder.method;
        this.pathMap = builder.pathMap;
        this.pathList = builder.pathList;
        this.isInBody = builder.isInBody;
        this.activity = builder.activity;
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

    public IHttpHeaderListener headerListener() {
        return headerListener;
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

    public HashMap<String, String> pathMap() {
        return pathMap;
    }

    public ArrayList<String> pathList() {
        return pathList;
    }

    public boolean isInBody() {
        return isInBody;
    }

    public String method() {
        return method;
    }

    public Activity activity() {
        return activity;
    }

    public static class Builder {
        private RClient rClient;
        private IHttpRespListener respListener;
        private IHttpHeaderListener headerListener;
        private Object reqParams;
        private String url;
        private HashMap<String, String> headerMap;
        private String method;
        private Map<String, Object> params;
        private HashMap<String, String> pathMap;
        private ArrayList<String> pathList;
        private boolean isInBody;
        private Activity activity;

        private Builder(String url) {
            this.url = url;
        }

        public static Builder create(@NonNull String url) {
            return new Builder(url);
        }

        public Builder(Request req) {
            this.rClient = req.rClient;
            this.respListener = req.respListener;
            this.headerListener = req.headerListener;
            this.reqParams = req.reqParams;
            this.url = req.url;
            this.headerMap = req.headerMap;
            this.method = req.method;
            this.pathMap = req.pathMap;
            this.pathList = req.pathList;
            this.isInBody = req.isInBody;
            this.activity = req.activity;
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
         */
        public Builder addParam(String name, Object value) {
            if (params == null) {
                params = new HashMap<>();
            }
            params.put(name, value);
            return this;
        }

        /**
         * 设置请求路径
         * @param path  动态追加url路径，兼容restful，该路径默认拼接到url后，
         *              如果要指定添加位置请使用{@link #setPath}
         */
        public Builder addPath(String path) {
            if (pathList == null) {
                pathList = new ArrayList<>();
            }
            pathList.add(path);
            return this;
        }

        /**
         * 设置请求路径
         * @param key    url路径中占位名称，key和占位名称必须一致，如: example/{id}/files
         * @param path  动态设置url路径，兼容restful
         */
        public Builder setPath(String key, String path) {
            if (pathMap == null) {
                pathMap = new HashMap<>();
            }
            pathMap.put(key, path);
            return this;
        }

        /**
         * 设置传参数形式
         * @param isInBody  true参数存在请求体body中， false参数拼接到url后，和get请求一样，
         *                  post和get请求无需设置此值，使用put，delete，patch等方法时，请设置此值，否则默认为false；
         */
        public Builder setIsInBody(boolean isInBody) {
            this.isInBody = isInBody;
            return this;
        }

        public Builder setActivity(Activity activity) {
            this.activity = activity;
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
         * 设置响应头监听器
         * @param listener 响应头回调接口, 返回全部响应头信息
         */
        public Builder headerListener(IHttpHeaderListener listener) {
            this.headerListener = listener;
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
         * 批量设置请求头属性,将请求头以key-value的形式装入map，如<p>
         *     {@code headerMap.put("Content-Type", "application/x-www-form-urlencoded");}
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
         * @param method 请求方法，不设置默认为GET, 必须大写, 建议使用{@link ReqMethod}传参
         */
        public Builder sendReq(String method) {
            if (TextUtils.isEmpty(method)) {
                this.method = ReqMethod.GET.getMethod();
            } else {
                this.method = method;
            }
            if (this.method.equalsIgnoreCase(ReqMethod.POST.getMethod())) {
                isInBody = true;
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
