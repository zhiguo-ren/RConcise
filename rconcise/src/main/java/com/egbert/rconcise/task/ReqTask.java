package com.egbert.rconcise.task;

import android.text.TextUtils;
import android.util.Patterns;

import com.egbert.rconcise.internal.Const;
import com.egbert.rconcise.internal.ContentType;
import com.egbert.rconcise.internal.HeaderField;
import com.egbert.rconcise.RConcise;
import com.egbert.rconcise.internal.ReqMethod;
import com.egbert.rconcise.internal.Utils;
import com.egbert.rconcise.internal.http.Request;
import com.egbert.rconcise.service.IReqService;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Created by Egbert on 2/25/2019.
 */
public class ReqTask implements Runnable {
    private IReqService reqService;
    private Request request;

    public ReqTask(Request request) {
        this.request = request;
        reqService = request.getReqService();
        reqService.setRespListener(request.getRespListener());
        Map<String, String> headerMap = request.getHeaderMap();
        String method = request.getMethod();
        Object reqParams = request.getReqParams();
        String url = request.getUrl();
        reqService.setReqMethod(method);
        reqService.setHeaderMap(headerMap);
        String baseUrl = RConcise.inst().getBaseUrl();
        String finalUrl;
        if (TextUtils.isEmpty(baseUrl) && Utils.verifyUrl(url, false)) {
            finalUrl = url;
        } else {
            if (url == null) {
                throw new IllegalArgumentException("The url cannot be null.");
            }
            if (Patterns.WEB_URL.matcher(url).matches()) {
                finalUrl = url;
            } else {
                if (url.startsWith(Const.HTTP_SEPARATOR)) {
                    throw new IllegalArgumentException("The url cannot start with '/'");
                }
                finalUrl = baseUrl + url;
            }
        }
        reqService.setUrl(finalUrl);
        if (reqParams != null) {
            String contentType = null;
            if (headerMap != null) {
                contentType = headerMap.get(HeaderField.CONTENT_TYPE.getValue());
            }
            String type = ContentType.FORM_URLENCODED.getContentType();
            if (method.equalsIgnoreCase(ReqMethod.GET.getMethod())) {
                StringBuilder builder = parseParams(reqParams);
                if (builder != null && builder.length() > 0) {
                    builder.insert(0, "?");
                    reqService.setUrl(finalUrl + builder.toString());
                }
            } else if (TextUtils.isEmpty(contentType) || type.equalsIgnoreCase(contentType)
                    || type.substring(0, type.lastIndexOf(";")).equalsIgnoreCase(contentType)) {
                StringBuilder builder = parseParams(reqParams);
                if (builder != null && builder.length() > 0) {
                    reqService.setReqParams(builder.toString().getBytes(StandardCharsets.UTF_8));
                }
            } else if (ContentType.JSON.getContentType().equalsIgnoreCase(contentType)) {
                String json = new Gson().toJson(reqParams);
                reqService.setReqParams(json.getBytes(StandardCharsets.UTF_8));
            } else {
                reqService.setReqParams(reqParams.toString().getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    private StringBuilder parseParams(Object reqParams) {
        Map<String, String> map;
        if (reqParams instanceof Map) {
            map = (Map<String, String>) reqParams;
        } else {
            map = Utils.beanToMap(reqParams);
        }
        StringBuilder builder = null;
        if (map != null && map.size() != 0) {
            builder = new StringBuilder();
            for (Map.Entry entry : map.entrySet()) {
                builder.append(entry.getKey())
                        .append("=")
                        .append(entry.getValue())
                        .append("&");
            }
            builder.deleteCharAt(builder.lastIndexOf("&"));
        }
        return builder;
    }

    @Override
    public void run() {
        reqService.execute();
    }

}
