package com.ardverk.dht2;

import java.util.concurrent.TimeUnit;

public class ConfigUtils {

    public ConfigUtils() {}
    
    public static long getTimeoutInMillis(Config... configs) {
        return getTimeout(TimeUnit.MILLISECONDS, configs);
    }
    
    public static long getTimeout(TimeUnit unit, Config... configs) {
        long time = 0;
        
        for (Config config : configs) {
            time += config.getTimeout(unit);
        }
        
        return time;
    }
}
