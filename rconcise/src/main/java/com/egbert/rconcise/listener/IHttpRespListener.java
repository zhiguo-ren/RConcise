package com.egbert.rconcise.listener;

import java.util.List;
import java.util.Map;

/**
 * Created by Egbert on 2/25/2019.
 * 请求结果的处理
 */
public interface IHttpRespListener {

    /**
     * 成功返回
     */
    void onSuccess(String resp, Map<String, List<String>> headerMap);

    /**
     * 发生错误
     */
    void onError(Exception e);

    /**
     * 请求失败
     */
    void onFailure(int respCode, String desp);

}
