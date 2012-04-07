package org.ardverk.dht.storage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.rsrc.ByteArrayValue;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.dht.rsrc.Value;
import org.ardverk.io.IoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A very simple implementation of {@link Datastore}.
 */
public class TransientDatastore extends SimpleDatastore {

  private static final Logger LOG 
    = LoggerFactory.getLogger(TransientDatastore.class);
  
  private final Map<Key, Handle> map = new ConcurrentHashMap<>();
  
  public TransientDatastore(long frequency, TimeUnit unit) {
    super(frequency, frequency, unit);
  }
  
  public TransientDatastore(long frequency, long timeout, final TimeUnit unit) {
    super(frequency, timeout, unit);
  }
  
  @Override
  protected void evict(long timeout, TimeUnit unit) {
    long now = System.currentTimeMillis();
    long timeoutInMillis = unit.toMillis(timeout);
    
    for (Handle handle : map.values()) {
      if ((now - handle.creationTime) >= timeoutInMillis) {
        map.remove(handle.key);
      }
    }
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
