package com.egbert.rconcise.service;

import com.egbert.rconcise.internal.http.IRequest;

/**
 * Created by Egbert on 2/25/2019.
 * 设置请求参数及请求逻辑
 */
public interface IReqService {

    /**
     * 配置请求Request
     */
    void setRequest(IRequest request);

    /**
     * 执行请求
     */
    void execute();

}
