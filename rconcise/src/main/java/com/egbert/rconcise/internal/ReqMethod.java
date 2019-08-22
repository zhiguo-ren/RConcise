package com.egbert.rconcise.internal;

/**
 * Created by Egbert on 2/25/2019.
 * 请求方法
 */
public enum ReqMethod {
    /**
     * get请求
     */
    GET         ("GET"),
    /**
     * post 请求
     */
    POST        ("POST"),

    /**
     * head 请求
     */
    HEAD        ("HEAD"),

    /**
     * put 请求
     */
    PUT        ("PUT"),

    /**
     * delete 请求
     */
    DELETE     ("DELETE"),

    /**
     * patch 请求
     */
    PATCH     ("PATCH");

    private String method;

    ReqMethod(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }
}
