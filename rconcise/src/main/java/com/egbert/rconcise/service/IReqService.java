package com.egbert.rconcise.service;

import com.egbert.rconcise.listener.IHttpRespListener;

import java.util.Map;

/**
 * Created by Egbert on 2/25/2019.
 * 设置请求参数及请求逻辑
 */
public interface IReqService {
    /**
     * 设置请求url
     */
    void setUrl(String url);

    /**
     * 设置请求头信息
     * @param header
     */
    void setHeaderMap(Map<String, String> header);

    /**
     * 设置请求方法
     * @param reqMethod
     */
    void setReqMethod(String reqMethod);

    /**
     * 执行请求
     */
    void execute();

    /**设置响应数据回调接口
     * @param respListener 响应数据处理接口
     */
    void setRespListener(IHttpRespListener respListener);

    /**
     * 设置请求参数
     */
    void setReqParams(byte[] params);
}
