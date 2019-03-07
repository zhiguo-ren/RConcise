package com.egbert.rconcise.listener;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import java.util.List;
import java.util.Map;

import static com.egbert.rconcise.internal.Const.CALL_BACK_ERROR;
import static com.egbert.rconcise.internal.Const.CALL_BACK_FAILURE;
import static com.egbert.rconcise.internal.Const.CALL_BACK_SUCCESS;

/**
 * 该监听器返回string格式的响应结果，不做解析，由调用者自行解析，适用于非json格式的数据
 * Created by Egbert on 2/27/2019.
 */
public class StringRespListener implements IHttpRespListener {
    private IRespListener<String> dataRespListener;
    private Handler handler;

    public StringRespListener(IRespListener<String> dataRespListener) {
        this.dataRespListener = dataRespListener;
        this.handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onSuccess(String respStr, Map<String, List<String>> headerMap) {
        if (TextUtils.isEmpty(respStr)) {
            callback(CALL_BACK_ERROR, null, null, "数据为null", 0);
        } else {
            callback(CALL_BACK_SUCCESS, respStr, null, "", 0);
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

    private void callback(final int which, final String respStr, final Exception e,
                          final String desp, final int respCode) {
        if (dataRespListener != null) {
            switch (which) {
                case CALL_BACK_SUCCESS:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            dataRespListener.onSuccess(respStr);
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
