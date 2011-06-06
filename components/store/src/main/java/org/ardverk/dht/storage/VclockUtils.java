package org.ardverk.dht.storage;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.apache.http.Header;
import org.ardverk.version.Occured;

public class VclockUtils {
    
    private VclockUtils() {}
    
    public static <K> Occured compare(Vclock existing, 
            Vclock clock) {
        
        if (existing == null || existing.isEmpty()
                || clock == null || clock.isEmpty()) {
            return Occured.AFTER;
        }
        
        return clock.compareTo(existing);
    }
    
    public static Vclock valueOf(Context context) throws IOException {
        Header clientId = context.removeHeader(Constants.CLIENT_ID);
        if (clientId == null) {
            throw new NoSuchElementException(Constants.CLIENT_ID);
        }
        
        return getOrCreate(context).update(clientId.getValue());
    }
    
    private static Vclock getOrCreate(Context context) throws IOException {
        Header vclock = context.removeHeader(Constants.VCLOCK);
        
        if (vclock != null) {
            return Vclock.valueOf(vclock.getValue());
        }
        
        return Vclock.create(); // Create a new Vclock
    }
}
