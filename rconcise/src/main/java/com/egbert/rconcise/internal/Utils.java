package com.egbert.rconcise.internal;

import android.os.Handler;
import android.os.Looper;
import android.util.Patterns;

import com.egbert.rconcise.listener.IRespListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Egbert on 2/25/2019.
 */
public class Utils {

    public static String handleInputStream(InputStream is, final IRespListener listener) {
        Handler handler = new Handler(Looper.getMainLooper());
        if (is != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder builder = new StringBuilder();
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append("\n");
                }
                return builder.toString();
            } catch (final IOException e) {
                if (listener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onError(e, "");
                        }
                    });
                }
                e.printStackTrace();
            } finally {
                try {
                    reader.close();
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
            throw new IllegalArgumentException("The url cannot be null.");
        }
        if (!Patterns.WEB_URL.matcher(url).matches()) {
            throw new IllegalArgumentException("The url is illegal.");
        }
        if (isBase && !url.endsWith(Const.HTTP_SEPARATOR)) {
            throw new IllegalArgumentException("The baseUrl must to end with '/'");
        }
        return true;
    }
}
