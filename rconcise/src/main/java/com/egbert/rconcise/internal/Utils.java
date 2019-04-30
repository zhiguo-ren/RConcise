package com.egbert.rconcise.internal;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Patterns;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Egbert on 2/25/2019.
 */
public class Utils {
    public static final String TAG = "RConcise_Log";

    public static String handleInputStream(InputStream is) throws IOException {
        if (is != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder builder = new StringBuilder();
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append("\n");
                }
                return builder.toString();
            } finally {
                reader.close();
                is.close();
            }
        }
        return null;
    }

    /**
      * 实体对象转Map
      * @param obj 实体对象
      * @return
      */
    public static Map<String, String> beanToMap(Object obj) {
        Map<String, String> map = new HashMap<>();
        if (obj == null) {
            return map;
        }
        Class clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(obj);
                if (value != null && !"".equals(value)) {
                    map.put(field.getName(), String.valueOf(value));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public static boolean verifyUrl(String url, boolean isBase) {
        if (url == null) {
            throw new IllegalArgumentException("The Url cannot be null.");
        }
        if (url.startsWith(Const.HTTP_SEPARATOR)) {
            throw new IllegalArgumentException("The Url cannot to start with '/'");
        }
        if (isBase && !url.endsWith(Const.HTTP_SEPARATOR)) {
            throw new IllegalArgumentException("The baseUrl must to end with '/'");
        }
        return Patterns.WEB_URL.matcher(url).matches();
    }

    /**
     * 解析请求参数
     */
    public static StringBuilder parseParams(Object reqParams, boolean isGet) throws UnsupportedEncodingException {
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
                        .append(isGet ? URLEncoder.encode(String.valueOf(entry.getValue()),
                                Const.UTF8) : entry.getValue())
                        .append("&");
            }
            builder.deleteCharAt(builder.lastIndexOf("&"));
        }
        return builder;
    }

    public static <T> T checkNotNull(final T obj) {
        if (obj == null) {
            throw new NullPointerException();
        }
        return obj;
    }

    public static byte[] paramsToByte(String contentType, Object reqParams) throws UnsupportedEncodingException {
        String tmpType = ContentType.FORM_URLENCODED.getValue();
        byte[] params = null;
        if (TextUtils.isEmpty(contentType)) {
            contentType = tmpType;
        }
        if (tmpType.contains(contentType)) {
            StringBuilder builder = Utils.parseParams(reqParams, false);
            if (builder != null && builder.length() > 0) {
                params = builder.toString().getBytes(Const.UTF8);
            }
        } else if (ContentType.JSON.getValue().contains(contentType)) {
            String json = new Gson().toJson(reqParams);
            params = json.getBytes(Const.UTF8);
        } else {
            params = reqParams.toString().getBytes(Const.UTF8);
        }
        return params;
    }

    public static String guessFileName(String url) throws UnsupportedEncodingException, IllegalStateException {
        String filename = null;
        String decodedUrl = Uri.decode(url);
        if (decodedUrl != null) {
            int queryIndex = decodedUrl.indexOf('?');
            // If there is a query string strip it, same as desktop browsers
            if (queryIndex > 0) {
                decodedUrl = decodedUrl.substring(0, queryIndex);
            }
            if (!decodedUrl.endsWith("/")) {
                int index = decodedUrl.lastIndexOf('/') + 1;
                if (index > 0) {
                    filename = URLDecoder.decode(decodedUrl.substring(index), Const.UTF8);
                }
            }
        }
        if (TextUtils.isEmpty(filename) || !filename.contains(".")) {
            throw new IllegalStateException("The filename cannot be obtained from the url, please set filename.");
        }
        return filename;
    }
}
