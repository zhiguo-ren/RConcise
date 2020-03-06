package com.egbert.rconcise.listener;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.List;
import java.util.Map;

import static com.egbert.rconcise.internal.Const.CALL_BACK_ERROR;
import static com.egbert.rconcise.internal.Const.CALL_BACK_FAILURE;
import static com.egbert.rconcise.internal.Const.CALL_BACK_SUCCESS;

/**
 * Created by Egbert on 2/25/2019.
 * json数据格式处理
 */
public class JsonRespListenerImpl<T> implements IHttpRespListener {
    private Class<T> respType;
    private IRespListener<T> dataRespListener;
    private Handler handler;

    public JsonRespListenerImpl(Class<T> respType, IRespListener<T> dataRespListener) {
        this.respType = respType;
        this.dataRespListener = dataRespListener;
        this.handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onSuccess(String resp, Map<String, List<String>> headerMap) {
        if (TextUtils.isEmpty(resp)) {
            callback(CALL_BACK_ERROR, null, null, "数据为null", 0);
        } else {
            try {
                T model = new Gson().fromJson(resp, respType);
                callback(CALL_BACK_SUCCESS, model, null, "", 0);
            } catch (JsonSyntaxException e) {
                callback(CALL_BACK_ERROR, null, e, "非法的json数据", 0);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onError(Exception e) {
        callback(CALL_BACK_ERROR, null, e, e.getMessage(), 0);
    }

    @Override
    public void onFailure(int respCode, String desp) {
        callback(CALL_BACK_FAILURE, null, null, desp, respCode);
    }

    private void callback(final int which, final T model, final Exception e,
                          final String desp, final int respCode) {
        if (dataRespListener != null) {
            switch (which) {
                case CALL_BACK_SUCCESS:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            dataRespListener.onSuccess(model);
                        }
                    });
                    break;
                case CALL_BACK_ERROR:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            dataRespListener.onError(e, desp);
                        }
                    });
                    break;
                case CALL_BACK_FAILURE:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            dataRespListener.onFailure(respCode, desp);
                        }
                    });
                    break;
                default: break;
            }
        }
    }
}
