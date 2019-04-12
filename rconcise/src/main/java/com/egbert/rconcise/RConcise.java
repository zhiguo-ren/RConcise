package com.egbert.rconcise;

import java.util.HashMap;

/**
 * Created by Egbert on 2/25/2019.
 */
public class RConcise {

    private HashMap<String, RClient> rClientHashMap;

    private static volatile RConcise sRConcise;

    private RConcise() {
        rClientHashMap = new HashMap<>();
    }

    public static RConcise inst() {
        if (sRConcise == null) {
            synchronized (RConcise.class) {
                if (sRConcise == null) {
                    sRConcise = new RConcise();
                }
            }
        }
        return sRConcise;
    }

    /**
     * 创建请求客户端
     * @param name  名称标识
     */
    public RClient createRClient(String name) {
        if (rClientHashMap.containsKey(name)) {
            throw new IllegalArgumentException("This name is existed.");
        }
        RClient rClient = new RClient();
        rClientHashMap.put(name, rClient);
        return rClient;
    }

    /**
     * 通过名称获取请求Client
     * @param name 名称
     */
    public RClient rClient(String name) {
        return rClientHashMap.get(name);
    }

    public void removeRClient(String name) {
        rClientHashMap.remove(name);
    }

    public void removeAllRClient() {
        rClientHashMap.clear();
    }

}
