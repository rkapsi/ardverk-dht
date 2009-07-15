package com.ardverk.dht.routing;

import java.net.SocketAddress;
import java.util.Map;

import com.ardverk.dht.KUID;

public interface Contact {
    
    public static enum Type {
        CHARTED,
        UNCHARTED
    }
    
    public long getCreationTime();

    public long getTimeStamp();
    
    public KUID getContactId();
    
    public int getInstanceId();
    
    public SocketAddress getRemoteAddress();
    
    public Type getType();
    
    public Object getAttribute(Object key);
    
    public Object setAttribute(Object key, Object value);
    
    public Object removeAttribute(Object key);
    
    public boolean hasAttribute(Object key);
    
    public Map<Object, Object> getAttributes();
    
    public boolean isCharted();
}
