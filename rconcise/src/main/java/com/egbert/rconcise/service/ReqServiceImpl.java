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
import com.egbert.rconcise.listener.IHttpHeaderListener;
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
    private IHttpHeaderListener headerListener;
    private Response response;

    @Override
    public void setRequest(IRequest request) {
        this.request = (Request) request;
        httpRespListener = this.request.respListener();
        headerListener = this.request.headerListener();
    }

    @Override
    public void execute() {
        try {
            response = getResponseByInterceptors();
            if (response == null) {
                return;
            }
            int respCode = response.respCode();
            String respStr = response.respStr();
            //响应头
            Map<String, List<String>> headerMap = response.headers();
            if (headerListener != null && !Utils.isFinishActivity(response.request().activity())) {
                headerListener.onHeaders(headerMap);
            }
            if (httpRespListener != null) {
                if (respCode == HttpURLConnection.HTTP_OK
                        || respCode == HttpURLConnection.HTTP_CREATED
                        || respCode == HttpURLConnection.HTTP_NO_CONTENT) {
                    if (!Utils.isFinishActivity(response.request().activity())) {
                        httpRespListener.onSuccess(respStr, headerMap);
                    }
                } else if (respCode >= HttpURLConnection.HTTP_INTERNAL_ERROR) {
                    if (!Utils.isFinishActivity(response.request().activity())) {
                        httpRespListener.onFailure(respCode, TextUtils.isEmpty(respStr) ? response.message() : respStr);
                    }
                } else {
                    if (!Utils.isFinishActivity(response.request().activity())) {
                        httpRespListener.onFailure(respCode, response.message());
                    }
                }
            }
        } catch (Exception e) {
            if (httpRespListener != null && !Utils.isFinishActivity(response.request().activity())) {
                httpRespListener.onError(e);
            }
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
