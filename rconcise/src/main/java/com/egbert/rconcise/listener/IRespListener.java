package com.egbert.rconcise.listener;

/**
 * 调用层的数据响应接口
 * Created by Egbert on 2/25/2019.
 * @param <R> 返回数据类型
 */
public interface IRespListener<R> {

    void onSuccess(R r);

    void onError(Exception e, String desp);

    void onFailure(int respCode, String desp);
}
