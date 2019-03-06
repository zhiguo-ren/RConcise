package com.egbert.rconcise.service;

import android.text.TextUtils;

import com.egbert.rconcise.interceptor.CallNetServiceInterceptor;
import com.egbert.rconcise.interceptor.Interceptor;
import com.egbert.rconcise.interceptor.InterceptorChainImpl;
import com.egbert.rconcise.interceptor.UrlProcessInterceptor;
import com.egbert.rconcise.internal.http.Request;
import com.egbert.rconcise.internal.http.Response;
import com.egbert.rconcise.listener.IHttpRespListener;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Egbert on 2/25/2019.
 */
public class ReqServiceImpl implements IReqService {

    private Request request;
    private IHttpRespListener httpRespListener;
    private Response response;

    public void setRequest(Request request) {
        this.request = request;
        httpRespListener = request.respListener();
    }

    @Override
    public void execute() {
        try {
            response = getResponseByInterceptors();
            if (response.exception() != null) {
                httpRespListener.onError(response.exception());
                return;
            }
            int respCode = response.respCode();
            String respStr = response.respStr();
            if (respCode == HttpURLConnection.HTTP_OK || respCode == HttpURLConnection.HTTP_PARTIAL) {
                //响应头
                Map<String, List<String>> headerMap = response.headers();
                httpRespListener.onSuccess(respStr, headerMap);
            } else if (respCode >= HttpURLConnection.HTTP_INTERNAL_ERROR) {
                httpRespListener.onFailure(respCode, TextUtils.isEmpty(respStr) ? response.message() : respStr);
            } else {
                httpRespListener.onFailure(respCode, response.message());
            }
        } catch (IOException e) {
            httpRespListener.onError(e);
            e.printStackTrace();
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
