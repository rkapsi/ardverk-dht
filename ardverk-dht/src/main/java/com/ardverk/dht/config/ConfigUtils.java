package com.ardverk.dht.config;

import java.util.concurrent.TimeUnit;

public class ConfigUtils {

    public ConfigUtils() {}
    
    public static long getOperationTimeoutInMillis(Config... configs) {
        return getOperationTimeout(configs, TimeUnit.MILLISECONDS);
    }
    
    public static long getOperationTimeout(Config[] configs, TimeUnit unit) {
        long time = 0;
        
        for (Config config : configs) {
            time += config.getOperationTimeout(unit);
        }
        
        return time;
    }
}
