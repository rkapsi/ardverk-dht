package com.ardverk.dht.routing;

import java.net.SocketAddress;
import java.util.Map;

import com.ardverk.dht.KUID;

public interface Contact {
    
    public static enum State {
        UNKNOWN,
        ALIVE,
        DEAD
    }
    
    public long getCreationTime();

    public long getTimeStamp();
    
    public KUID getContactId();
    
    public int getInstanceId();
    
    public SocketAddress getRemoteAddress();
    
    public State getState();
    
    public Contact changeState(State state);
    
    public Object getAttribute(Object key);
    
    public Object setAttribute(Object key, Object value);
    
    public Object removeAttribute(Object key);
    
    public boolean hasAttribute(Object key);
    
    public Map<Object, Object> getAttributes();
}
