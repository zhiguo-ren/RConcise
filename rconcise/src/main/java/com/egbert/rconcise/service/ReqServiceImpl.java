package com.egbert.rconcise.service;

import android.text.TextUtils;
import android.util.Log;

import com.egbert.rconcise.interceptor.CallNetServiceInterceptor;
import com.egbert.rconcise.interceptor.Interceptor;
import com.egbert.rconcise.interceptor.InterceptorChainImpl;
import com.egbert.rconcise.interceptor.UrlProcessInterceptor;
import com.egbert.rconcise.internal.Utils;
import com.egbert.rconcise.internal.http.IRequest;
import com.egbert.rconcise.internal.http.Request;
import com.egbert.rconcise.internal.http.Response;
import com.egbert.rconcise.listener.IHttpRespListener;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * get，post请求，服务类
 * Created by Egbert on 2/25/2019.
 */
public class ReqServiceImpl implements IReqService {

    private Request request;
    private IHttpRespListener httpRespListener;
    private Response response;

    @Override
    public void setRequest(IRequest request) {
        this.request = (Request) request;
        httpRespListener = this.request.respListener();
    }

    @Override
    public void execute() {
        try {
            response = getResponseByInterceptors();
            int respCode = response.respCode();
            String respStr = response.respStr();
            if (respCode == HttpURLConnection.HTTP_OK) {
                //响应头
                Map<String, List<String>> headerMap = response.headers();
                httpRespListener.onSuccess(respStr, headerMap);
            } else if (respCode >= HttpURLConnection.HTTP_INTERNAL_ERROR) {
                httpRespListener.onFailure(respCode, TextUtils.isEmpty(respStr) ? response.message() : respStr);
            } else {
                httpRespListener.onFailure(respCode, response.message());
            }
        } catch (Exception e) {
            httpRespListener.onError(e);
            Log.e(Utils.TAG, Log.getStackTraceString(e));
        }
    }

    private Response getResponseByInterceptors() throws IOException {
        ArrayList<Interceptor> interceptors = new ArrayList<>();
        ArrayList<Interceptor> clientInterceptors = request.rClient().getInterceptors();
        interceptors.add(new UrlProcessInterceptor());
        if (clientInterceptors != null) {
            interceptors.addAll(clientInterceptors);
        }
        interceptors.add(new CallNetServiceInterceptor());
        InterceptorChainImpl chain = new InterceptorChainImpl(interceptors, request, 0);
        return chain.proceed(request);
    }
}
