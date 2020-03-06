package com.egbert.rconcise.listener;

import java.util.List;
import java.util.Map;

/**
 * @author Egbert
 * @date 8/22/2019
 */
public interface IHttpHeaderListener {
    void onHeaders(Map<String, List<String>> headers);
}
