package com.egbert.rconcise.internal;

/**
 * Created by Egbert on 2/25/2019.
 */
public enum ContentType {
    /**
     * form 表单
     */
    FORM_URLENCODED     ("application/x-www-form-urlencoded;charset=utf-8"),
    /**
     * form-data
     */
    MULTIPART           ("multipart/form-data;"),
    /**
     * json
     */
    JSON                ("application/json;charset=utf-8"),
    /**
     * 纯文本
     */
    PLAIN               ("text/plain;charset=utf-8");

    private String contentType;

    ContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return this.contentType;
    }
}
