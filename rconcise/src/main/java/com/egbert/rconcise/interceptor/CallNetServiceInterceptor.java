package com.egbert.rconcise.interceptor;

import com.egbert.rconcise.internal.ContentType;
import com.egbert.rconcise.internal.HeaderField;
import com.egbert.rconcise.internal.ReqMethod;
import com.egbert.rconcise.internal.Utils;
import com.egbert.rconcise.internal.http.Request;
import com.egbert.rconcise.internal.http.Response;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Egbert on 3/5/2019.
 */
public class CallNetServiceInterceptor implements Interceptor {
    private static final String CONTENT_TYPE = "Content-Type";
    private Response.Builder builder = Response.Builder.create();

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Map<String, String> headerMap = request.headers();
        Object reqParams = request.reqParams();

        byte[] params = null;
        if (request.method().equalsIgnoreCase(ReqMethod.POST.getMethod()) && reqParams != null) {
            String contentType = null;
            if (headerMap != null) {
                contentType = headerMap.get(HeaderField.CONTENT_TYPE.getValue());
            }
            params = Utils.paramsToByte(contentType, reqParams);
        }

        HttpURLConnection connection = null;
        try {
            URL reqUrl = new URL(request.url());
            connection = (HttpURLConnection) reqUrl.openConnection();
            connection.setRequestMethod(request.method());
            if (request.method().equalsIgnoreCase(ReqMethod.POST.getMethod())) {
                connection.setDoOutput(true);
            }
            //默认Content-Type值为application/x-www-form-urlencoded
            connection.setRequestProperty(CONTENT_TYPE, ContentType.FORM_URLENCODED.getContentType());
            HashMap<String, String> headers = request.headers();
            if (headers != null && headers.size() != 0) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            builder.reqStartTime(System.currentTimeMillis());
            connection.connect();
            if (request.method().equalsIgnoreCase(ReqMethod.POST.getMethod()) && params != null) {
                BufferedOutputStream writer = new BufferedOutputStream(connection.getOutputStream());
                writer.write(params);
                writer.close();
            }
            int code = connection.getResponseCode();
            String resp = Utils.handleInputStream(connection.getInputStream());
            builder.code(code)
                    .respEndTime(System.currentTimeMillis())
                    .message(connection.getResponseMessage())
                    .headers(connection.getHeaderFields())
                    .respStr(resp)
                    .request(request);
            return builder.build();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

}