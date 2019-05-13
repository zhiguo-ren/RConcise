package com.egbert.rconcise;

import com.egbert.rconcise.interceptor.Interceptor;
import com.egbert.rconcise.internal.Utils;

import java.util.ArrayList;

/**
 * 网络请求客户端，用于配置网络请求的公用{@code baseUrl}和添加拦截器（可用于拦截请求，对请求和响应进行附加操作）.
 * <p>
 * 如果app内有多个{@code baseUrl}（比如会调用不同的web服务端提供的接口），可以创建多个{@code RClient}实例，
 * 通过{@code Request}(参见{@link com.egbert.rconcise.internal.http.Request Request})在请求时指定具体使用的
 * {@code RClient}，适配多{@code baseUrl}的场景.
 * <p><br>
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

    /**
     * @param baseUrl 设置baseUrl
     */
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

    /**
     * @param interceptor 添加拦截器
     */
    public void addInterceptor(Interceptor interceptor) {
        if (interceptor != null) {
            if (interceptors == null) {
                interceptors = new ArrayList<>();
            }
            interceptors.add(interceptor);
        }
    }

    public ArrayList<Interceptor> getInterceptors() {
        return interceptors;
    }
}
