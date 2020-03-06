package com.egbert.rconcise.interceptor;

import com.egbert.rconcise.internal.http.Request;
import com.egbert.rconcise.internal.http.Response;

import java.io.IOException;

/**
 * 拦截器 可在请求或响应过程中做一些操作，如修改请求或响应，打印日志等
 * Created by Egbert on 3/1/2019.
 */
public interface Interceptor {

    Response intercept(Chain chain) throws IOException;

    interface Chain {
        Request request();

        Response proceed(Request request) throws IOException;
    }

}
