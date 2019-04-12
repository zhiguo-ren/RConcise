package com.egbert.rconcise.internal;

/**
 * Created by Egbert on 2/26/2019.
 * http协议 请求和响应头中的属性字段枚举类
 */
public enum HeaderField {
    /**
     * http 请求和响应头中常用的属性字段
     */
    ACCEPT                  ("Accept"),
    ACCEPT_CHARSET          ("Accept-Charset"),
    ACCEPT_ENCODING         ("Accept-Encoding"),
    ACCEPT_LANGUAGE         ("Accept-Language"),
    AUTHORIZATION           ("Authorization"),
    CONNECTION              ("Connection"),
    CONTENT_LENGTH          ("Content-Length"),
    CONTENT_DISPOSITION     ("Content-Disposition"),
    CONTENT_ENCODING        ("Content-Encoding"),
    CONTENT_TYPE            ("Content-Type"),
    CONTENT_RANGE           ("Content-Range"),
    CONTENT_LANGUAGE        ("Content-Language"),
    RANGE                   ("Range"),
    COOKIE                  ("Cookie"),
    FROM                    ("From"),
    HOST                    ("Host"),
    PRAGMA                  ("Pragma"),
    REFERER                 ("Referer"),
    USER_AGENT              ("User-Agent"),
    DATE                    ("Date"),
    SERVER                  ("Server"),
    EXPIRES                 ("Expires");

    private String value;

    HeaderField(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
