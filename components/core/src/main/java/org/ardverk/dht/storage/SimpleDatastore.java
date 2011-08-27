package org.ardverk.dht.storage;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.ExecutorUtils;
import org.ardverk.concurrent.FutureUtils;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.rsrc.ByteArrayValue;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.dht.rsrc.StringValue;
import org.ardverk.dht.rsrc.Value;
import org.ardverk.io.IoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A very simple implementation of {@link Datastore}.
 */
public class SimpleDatastore extends AbstractDatastore implements Closeable {

    private static final Logger LOG 
        = LoggerFactory.getLogger(SimpleDatastore.class);
    
    private static final ScheduledExecutorService EXECUTOR 
        = ExecutorUtils.newSingleThreadScheduledExecutor("SimpleDatastoreThread");
    
    public static final Value OK = new StringValue("OK");
    
    public static final Value NOT_FOUND = new StringValue("Not Found");
    
    public static final Value INTERNAL_ERROR = new StringValue("Internal Error");
    
    private final Map<Key, Handle> map = new ConcurrentHashMap<Key, Handle>();
    
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
                    long now = System.currentTimeMillis();
                    long timeoutInMillis = unit.toMillis(timeout);
                    
                    for (Handle handle : map.values()) {
                        if ((now - handle.creationTime) >= timeoutInMillis) {
                            map.remove(handle.key);
                        }
                    }
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
    
    @Override
    public Value store(Contact src, Key key, Value value) {
        Value response = OK;
        try {
            Handle handle = Handle.valueOf(key, value);
            
            if (handle.isEmpty()) {
                if (map.remove(key) != null) {
                    response = NOT_FOUND;
                }
            } else {
                map.put(key, handle);
            }
        } catch (IOException err) {
            LOG.error("IOException", err);
            response = INTERNAL_ERROR;
        }
        
        return response;
    }

    @Override
    public Value get(Contact src, Key key) {
        Handle handle = map.get(key);
        return handle != null ? handle.value : null;
    }
    
    @Override
    public String toString() {
        return map.values().toString();
    }
    
    private static class Handle {
        
        public static Handle valueOf(Key key, Value value) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            InputStream in = value.getContent();
            try {
                byte[] buffer = new byte[4*1024];
                int len = -1;
                while ((len = in.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                
                return new Handle(key, new ByteArrayValue(baos.toByteArray()));
            } finally {
                IoUtils.close(in);
            }
        }
        
        private final long creationTime = System.currentTimeMillis();
        
        private final Key key;
        
        private final ByteArrayValue value;
        
        private Handle(Key key, ByteArrayValue value) {
            this.key = key;
            this.value = value;
        }
        
        public boolean isEmpty() {
            return value.isEmpty();
        }
        
        @Override
        public String toString() {
            return key + "=" + value;
        }
    }
}
