package com.egbert.rconcise.internal;

import java.util.UUID;

/**
 * 常量类
 * Created by Egbert on 2/27/2019.
 */
public final class Const {
    public static final String UTF8 = "UTF-8";
    public static final String CHARTSET = "charset=utf-8";
    public static final String CHARTSET_LABEL = "charset";
    public static final String FORM_DATA = "form-data;";
    public static final String CRLF = "\r\n";
    public static final String BOUNDARY = UUID.randomUUID().toString();
    public static final String HTTP = "http://";
    public static final String HTTPS = "https";
    public static final String HTTP_SEPARATOR = "/";
    public static final String BOUNDARY_PREFIX = "--";

    public static final int CALL_BACK_SUCCESS = 1;
    public static final int CALL_BACK_ERROR = 2;
    public static final int CALL_BACK_FAILURE = 3;
}
