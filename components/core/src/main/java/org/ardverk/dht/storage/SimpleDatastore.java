package org.ardverk.dht.storage;

import java.io.Closeable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.ExecutorUtils;
import org.ardverk.concurrent.FutureUtils;
import org.ardverk.dht.rsrc.StringValue;
import org.ardverk.dht.rsrc.Value;

abstract class SimpleDatastore extends AbstractDatastore implements Closeable {

    private static final ScheduledExecutorService EXECUTOR 
        = ExecutorUtils.newSingleThreadScheduledExecutor("SimpleDatastoreThread");

    public static final Value OK = new StringValue("OK");
    
    public static final Value NOT_FOUND = new StringValue("Not Found");
    
    public static final Value INTERNAL_ERROR = new StringValue("Internal Error");
    
    private final ScheduledFuture<?> future;
    
    public SimpleDatastore(long frequency, TimeUnit unit) {
        this(frequency, frequency, unit);
    }
    
    public SimpleDatastore(long frequency, final long timeout, final TimeUnit unit) {
        
        ScheduledFuture<?> future = null;
        if (0L < frequency && 0L < timeout) {
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    evict(timeout, unit);
                }
            };
            
            future = EXECUTOR.scheduleWithFixedDelay(
                    task, frequency, frequency, unit);
        }
        
        this.future = future;
    }
    
    @Override
    public void close() {
        FutureUtils.cancel(future, true);
    }
    
    protected abstract void evict(long timeout, TimeUnit unit);
}
