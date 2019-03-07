package com.egbert.rconcise.interceptor;

import com.egbert.rconcise.internal.ReqMethod;
import com.egbert.rconcise.internal.Utils;
import com.egbert.rconcise.internal.http.Request;
import com.egbert.rconcise.internal.http.Response;

import java.io.IOException;

/**
 * 加工url的拦截器
 * Created by Egbert on 3/6/2019.
 */
public class UrlProcessInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException, RuntimeException {
        Request request = chain.request();
        String relativeUrl = request.url();
        String baseUrl = request.rClient().getBaseUrl();
        String absoluteUrl;
        if (Utils.verifyUrl(relativeUrl, false)) {
            absoluteUrl = relativeUrl;
        } else {
            if (Utils.verifyUrl(baseUrl, true)) {
                absoluteUrl = baseUrl + relativeUrl;
            } else {
                throw new IllegalArgumentException("The BaseUrl is illegal.");
            }
        }
        if (ReqMethod.GET.getMethod().equalsIgnoreCase(request.method())) {
            StringBuilder builder = Utils.parseParams(request.reqParams(), true);
            if (builder != null && builder.length() > 0) {
                builder.insert(0, "?");
                absoluteUrl += builder.toString();
            }
        }
        request = request.newBuilder().url(absoluteUrl).build();
        return chain.proceed(request);
    }
}
