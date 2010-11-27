package com.ardverk.dht.config;

import java.util.concurrent.TimeUnit;

class ConfigUtils {

    public ConfigUtils() {}
    
    /**
     * Returns the sum of the {@link Config}'s operation 
     * timeouts in milliseconds.
     */
    public static long getOperationTimeoutInMillis(Config... configs) {
        return getOperationTimeout(configs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Returns the sum of the {@link Config}'s operation timeouts 
     * in the given {@link TimeUnit}.
     */
    public static long getOperationTimeout(Config[] configs, TimeUnit unit) {
        long time = 0;
        
        for (Config config : configs) {
            time += config.getOperationTimeout(unit);
        }
        
        return time;
    }
}
