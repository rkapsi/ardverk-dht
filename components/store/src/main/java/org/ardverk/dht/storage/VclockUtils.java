package org.ardverk.dht.storage;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.apache.http.Header;
import org.ardverk.utils.ArrayUtils;
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
        Header[] clientIds = context.removeHeaders(Constants.CLIENT_ID);
        if (ArrayUtils.isEmpty(clientIds)) {
            throw new NoSuchElementException(Constants.CLIENT_ID);
        }
        
        Header[] vclocks = context.removeHeaders(Constants.VCLOCK);
        return valueOf(vclocks, clientIds);
    }
    
    private static Vclock valueOf(Header[] vclocks, Header[] clientIds) throws IOException {
        
        Vclock vclock = null;
        if (!ArrayUtils.isEmpty(vclocks)) {
            vclock = Vclock.valueOf(vclocks[0].getValue());
        } else {
            vclock = Vclock.create(); // Create a new Vclock
        }
        
        String clientId = clientIds[0].getValue();
        return vclock.update(clientId);
    }
}
