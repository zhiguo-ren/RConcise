package com.egbert.rconcise;

import com.egbert.rconcise.internal.Utils;

/**
 * Created by Egbert on 2/25/2019.
 */
public class RConcise {

    private static RConcise sRConcise;
    /**
     * baseUrl 为请求地址的公共前半部分，后边追加具体接口的路径，只需在app中设置一次，必须以'/'结尾；
     */
    private String baseUrl;

    private RConcise() {
    }

    public static synchronized RConcise inst() {
        if (sRConcise == null) {
            sRConcise = new RConcise();
        }
        return sRConcise;
    }

    public void setBaseUrl(String baseUrl) {
        if (Utils.verifyUrl(baseUrl, true)) {
            this.baseUrl = baseUrl;
        }
    }

    public String getBaseUrl() {
        return this.baseUrl;
    }

}
