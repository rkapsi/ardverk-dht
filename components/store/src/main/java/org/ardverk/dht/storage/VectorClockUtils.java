package org.ardverk.dht.storage;

import org.ardverk.version.Occured;
import org.ardverk.version.VectorClock;

public class VectorClockUtils {
    
    private VectorClockUtils() {}
    
    public static <K> Occured compare(VectorClock<K> existing, 
            VectorClock<K> clock) {
        
        if (existing == null || existing.isEmpty()
                || clock == null || clock.isEmpty()) {
            return Occured.AFTER;
        }
        
        return clock.compareTo(existing);
    }
}
