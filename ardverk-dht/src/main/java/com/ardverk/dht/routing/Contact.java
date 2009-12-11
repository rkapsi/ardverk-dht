package com.ardverk.dht.routing;

import java.net.Socket;
import java.net.SocketAddress;
import java.util.Map;

import com.ardverk.dht.KUID;

public interface Contact {
    
    public static enum Type {
        /**
         * {@link Contact}s that were returned in FIND_NODE responses
         */
        UNKNOWN(false),
        
        /**
         * {@link Contact}s that sent us a request
         */
        UNSOLICITED(true),
        
        /**
         * {@link Contact}s that sent us a response
         */
        SOLICITED(true);
        
        private final boolean active;
        
        private Type(boolean active) {
            this.active = active;
        }
        
        public boolean isActive() {
            return active;
        }
    }
    
    public long getCreationTime();

    public long getTimeStamp();
    
    public KUID getContactId();
    
    public int getInstanceId();
    
    /**
     * Returns the {@link SocketAddress} as reported by the {@link Socket}.
     */
    public SocketAddress getSocketAddress();
    
    /**
     * Returns the {@link SocketAddress} as reported in the 
     * requests and responses
     */
    public SocketAddress getContactAddress();
    
    /**
     * Returns the {@link SocketAddress} of the remote {@link Contact}
     */
    public SocketAddress getRemoteAddress();
    
    public Type getType();
    
    public Object getAttribute(Object key);
    
    public Object setAttribute(Object key, Object value);
    
    public Object removeAttribute(Object key);
    
    public boolean hasAttribute(Object key);
    
    public Map<Object, Object> getAttributes();
    
    public boolean isSolicited();
    
    public boolean isUnsolicited();
    
    public boolean isUnknown();
    
    public boolean isActive();
}
