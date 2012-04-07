/*
 * Copyright 2009-2012 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ardverk.dht.config;

import java.util.concurrent.TimeUnit;

import org.ardverk.dht.routing.Contact;

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
    
    public static long getAdaptiveTimeout(Contact dst, 
            double multiplier, long defaultTimeout, TimeUnit unit) {
        
        long rttInMillis = dst.getRoundTripTimeInMillis();
        if (0L < rttInMillis && 0d < multiplier) {
            long timeout = (long)(rttInMillis * multiplier);
            long adaptive = Math.min(timeout, 
                    unit.toMillis(defaultTimeout));
            return unit.convert(adaptive, TimeUnit.MILLISECONDS);
        }
        
        return defaultTimeout;
    }
}