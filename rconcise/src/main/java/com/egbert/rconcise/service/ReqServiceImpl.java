package com.egbert.rconcise.service;

import com.egbert.rconcise.internal.ContentType;
import com.egbert.rconcise.internal.ReqMethod;
import com.egbert.rconcise.listener.IHttpRespListener;

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Created by Egbert on 2/25/2019.
 */
public class ReqServiceImpl implements IReqService {
    private static final String CONTENT_TYPE = "Content-Type";
    private static String BASE_URL = "";
    private IHttpRespListener httpRespListener;
    private HttpURLConnection connection;

    private String url;
    private byte[] reqParams;
    private String reqMethod;
    private Map<String, String> headerMap;

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void setHeaderMap(Map<String, String> header) {
        this.headerMap = header;
    }

    @Override
    public void setReqMethod(String reqMethod) {
        this.reqMethod = reqMethod;
    }

    @Override
    public void execute() {
        try {
            URL reqUrl = new URL(url);
            connection = (HttpURLConnection) reqUrl.openConnection();
            connection.setRequestMethod(reqMethod);
            //默认Content-Type值为application/x-www-form-urlencoded
            connection.setRequestProperty(CONTENT_TYPE, ContentType.FORM_URLENCODED.getContentType());

            if (headerMap != null && headerMap.size() != 0) {
                for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            connection.connect();

            if (reqMethod.equalsIgnoreCase(ReqMethod.POST.getMethod())) {
                BufferedOutputStream writer = new BufferedOutputStream(connection.getOutputStream());
                writer.write(reqParams);
                writer.close();
            }

            int respCode = connection.getResponseCode();
            if (respCode == HttpURLConnection.HTTP_OK || respCode == HttpURLConnection.HTTP_PARTIAL) {
                InputStream inputStream = connection.getInputStream();
                //响应头map
                Map<String, List<String>> headerMap = connection.getHeaderFields();
                httpRespListener.onSuccess(inputStream, headerMap);
            } else {
                httpRespListener.onFailure(respCode, connection.getResponseMessage());
            }
        } catch (Exception e) {
            httpRespListener.onError(e);
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @Override
    public void setRespListener(IHttpRespListener respListener) {
        httpRespListener = respListener;
    }

    @Override
    public void setReqParams(byte[] params) {
        reqParams = params;
    }
}
