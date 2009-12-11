package com.ardverk.dht.io;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.ardverk.dht.message.RequestMessage;

/**
 * 
 */
public class TimeoutIoException extends IOException {
    
    private static final long serialVersionUID = -7330783412590437071L;

    private final RequestMessage request;
    
    private final long time;
    
    private final TimeUnit unit;
    
    public TimeoutIoException(RequestMessage request, 
            long time, TimeUnit unit) {
        super (createMessage(request, time, unit));
        
        if (request == null) {
            throw new NullPointerException("request");
        }
        
        if (time < 0L) {
            throw new IllegalArgumentException("time=" + time);
        }
        
        if (unit == null) {
            throw new NullPointerException("unit");
        }
        
        this.request = request;
        this.time = time;
        this.unit = unit;
    }
    
    /**
     * 
     */
    public RequestMessage getRequestMessage() {
        return request;
    }
    
    /**
     * 
     */
    public long getTime(TimeUnit unit) {
        return unit.convert(time, this.unit);
    }
    
    /**
     * 
     */
    private static String createMessage(RequestMessage request, 
            long time, TimeUnit unit) {
        return request.getClass().getSimpleName() + " (" 
            + request.getAddress() + ", " + time + ", " + unit + ")";
    }
}
