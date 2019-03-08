package com.egbert.rconcise.internal;

import android.util.Patterns;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
                                StandardCharsets.UTF_8.name()) : entry.getValue())
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
}
