package com.egbert.rconcise;

import com.egbert.rconcise.interceptor.Interceptor;
import com.egbert.rconcise.internal.Utils;

import java.util.ArrayList;

/**
 * 网络请求客户端
 * Created by Egbert on 3/5/2019.
 */
public class RClient {
    /**
     * baseUrl 为请求地址的公共前半部分，后边追加具体接口的路径，只需在app中设置一次，必须以'/'结尾；
     */
    private String baseUrl;

    /**
     * 客户端配置的拦截器
     */
    private ArrayList<Interceptor> interceptors;

    public void setBaseUrl(String baseUrl) {
        if (Utils.verifyUrl(baseUrl, true)) {
            this.baseUrl = baseUrl;
        } else {
            throw new IllegalArgumentException("The BaseUrl is illegal.");
        }
    }

    public String getBaseUrl() {
        return this.baseUrl;
    }

    public void setInterceptor(Interceptor interceptor) {
        if (interceptors == null) {
            interceptors = new ArrayList<>();
        }
        if (interceptor != null) {
            interceptors.add(interceptor);
        }
    }

    public ArrayList<Interceptor> getInterceptors() {
        return interceptors;
    }
}
