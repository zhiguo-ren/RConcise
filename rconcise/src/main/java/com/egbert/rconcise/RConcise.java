package com.egbert.rconcise;

import java.util.HashMap;

/**
 * Created by Egbert on 2/25/2019.
 */
public class RConcise {

    private HashMap<String, RClient> rClientHashMap;

    private static RConcise sRConcise;

    private RConcise() {
        rClientHashMap = new HashMap<>();
    }

    public static synchronized RConcise inst() {
        if (sRConcise == null) {
            sRConcise = new RConcise();
        }
        return sRConcise;
    }

    public RClient createRClient(String name) {
        if (rClientHashMap.containsKey(name)) {
            throw new IllegalArgumentException("This name is existed.");
        }
        RClient rClient = new RClient();
        rClientHashMap.put(name, rClient);
        return rClient;
    }

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
