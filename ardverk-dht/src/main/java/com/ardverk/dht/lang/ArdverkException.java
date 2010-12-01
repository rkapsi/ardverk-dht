package com.ardverk.dht.lang;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ArdverkException extends IOException {
    
    private static final long serialVersionUID = 5855991566361343341L;

    private final long timeInMillis;

    public ArdverkException(long time, TimeUnit unit) {
        this.timeInMillis = unit.toMillis(time);
    }

    public ArdverkException(String message, Throwable cause, 
            long time, TimeUnit unit) {
        super(message, cause);
        this.timeInMillis = unit.toMillis(time);
    }

    public ArdverkException(String message, long time, TimeUnit unit) {
        super(message);
        this.timeInMillis = unit.toMillis(time);
    }

    public ArdverkException(Throwable cause, long time, TimeUnit unit) {
        super(cause);
        this.timeInMillis = unit.toMillis(time);
    }
    
    public long getTime(TimeUnit unit) {
        return unit.convert(timeInMillis, TimeUnit.MILLISECONDS);
    }
    
    public long getTimeInMillis() {
        return getTime(TimeUnit.MILLISECONDS);
    }
}
