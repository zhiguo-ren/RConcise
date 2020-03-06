package com.egbert.rconcise.interceptor;

import android.text.TextUtils;

import com.egbert.rconcise.internal.Const;
import com.egbert.rconcise.internal.Utils;
import com.egbert.rconcise.internal.http.Request;
import com.egbert.rconcise.internal.http.Response;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;

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
        StringBuilder absoluteUrl;
        if (request.pathMap() != null && request.pathMap().size() > 0) {
            if (!TextUtils.isEmpty(relativeUrl)) {
                for (Map.Entry<String, String> entry : request.pathMap().entrySet()) {
                    relativeUrl = relativeUrl.replace("{" + entry.getKey() + "}",
                            URLEncoder.encode(String.valueOf(entry.getValue()),
                                    Const.UTF8));
                }
            }
        }
        if (Utils.verifyUrl(relativeUrl, false)) {
            absoluteUrl = new StringBuilder(relativeUrl);
        } else {
            if (Utils.verifyUrl(baseUrl, true)) {
                absoluteUrl = new StringBuilder(baseUrl + relativeUrl);
            } else {
                throw new IllegalArgumentException("The BaseUrl is illegal.");
            }
        }
        if (!TextUtils.isEmpty(absoluteUrl.toString())) {
            if (request.pathList() != null && request.pathList().size() > 0) {
                for (String s : request.pathList()) {
                    if (absoluteUrl.toString().endsWith(Const.HTTP_SEPARATOR)) {
                        absoluteUrl.append(s);
                    } else {
                        absoluteUrl.append(Const.HTTP_SEPARATOR).append(s);
                    }
                }
            }
        }
        if (!request.isInBody()) {
            StringBuilder builder = Utils.parseParams(request.reqParams(), false);
            if (builder != null && builder.length() > 0) {
                builder.insert(0, "?");
                absoluteUrl.append(builder.toString());
            }
        }
        request = request.newBuilder().url(absoluteUrl.toString()).build();
        return chain.proceed(request);
    }
}
