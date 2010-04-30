package com.ardverk.dht.io;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.ardverk.lang.NullArgumentException;

/**
 * 
 */
public class TimeoutIoException extends IOException {
    
    private static final long serialVersionUID = -7330783412590437071L;

    private final RequestEntity entity;
    
    private final long time;
    
    private final TimeUnit unit;
    
    public TimeoutIoException(RequestEntity entity, 
            long time, TimeUnit unit) {
        super (createMessage(entity, time, unit));
        
        if (entity == null) {
            throw new NullArgumentException("entity");
        }
        
        if (time < 0L) {
            throw new IllegalArgumentException("time=" + time);
        }
        
        if (unit == null) {
            throw new NullArgumentException("unit");
        }
        
        this.entity = entity;
        this.time = time;
        this.unit = unit;
    }
    
    /**
     * 
     */
    public RequestEntity getRequestEntity() {
        return entity;
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
    private static String createMessage(RequestEntity entity, 
            long time, TimeUnit unit) {
        return entity.getClass().getSimpleName() + " (" 
            + entity.getAddress() + ", " + time + ", " + unit + ")";
    }
}
