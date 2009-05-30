package com.ardverk.dht.routing;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ardverk.dht.KUID;

public class DefaultContact implements Contact {

    private final long creationTime;
    
    private final long timeStamp;
    
    private final KUID contactId;
    
    private final int instanceId;
    
    private final SocketAddress address;
    
    private final State state;
    
    private final Map<Object, Object> attributes 
        = new ConcurrentHashMap<Object, Object>();
    
    public DefaultContact(KUID contactId, int instanceId, 
            SocketAddress address, State state) {
        if (contactId == null) {
            throw new NullPointerException("contactId");
        }
        
        if (address == null) {
            throw new NullPointerException("address");
        }
        
        if (state == null) {
            throw new NullPointerException("state");
        }
        
        if (state == State.DEAD) {
            throw new IllegalArgumentException("state=" + state);
        }
        
        this.creationTime = System.currentTimeMillis();
        this.timeStamp = creationTime;
        
        this.contactId = contactId;
        this.instanceId = instanceId;
        this.address = address;
        this.state = state;
    }
    
    public DefaultContact(Contact existing, Contact contact) {
        
        if (existing == null) {
            throw new NullPointerException("existing");
        }
        
        if (contact == null) {
            throw new NullPointerException("contact");
        }
        
        // 2nd argument must be older
        if (contact.getCreationTime() < existing.getCreationTime()) {
            throw new IllegalArgumentException();
        }
        
        this.creationTime = existing.getCreationTime();
        this.timeStamp = contact.getTimeStamp();
        
        this.contactId = existing.getContactId();
        this.instanceId = contact.getInstanceId();
        this.address = contact.getRemoteAddress();
        this.state = contact.getState();
        
        this.attributes.putAll(existing.getAttributes());
        this.attributes.putAll(contact.getAttributes());
    }
    
    @Override
    public long getCreationTime() {
        return creationTime;
    }
    
    @Override
    public long getTimeStamp() {
        return timeStamp;
    }

    @Override
    public KUID getContactId() {
        return contactId;
    }

    @Override
    public int getInstanceId() {
        return instanceId;
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return address;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public Object getAttribute(Object key) {
        return attributes.get(key);
    }

    @Override
    public boolean hasAttribute(Object key) {
        return attributes.containsKey(key);
    }

    @Override
    public Object removeAttribute(Object key) {
        return attributes.remove(key);
    }

    @Override
    public Object setAttribute(Object key, Object value) {
        return attributes.put(key, value);
    }

    @Override
    public Map<Object, Object> getAttributes() {
        return attributes;
    }

    @Override
    public int hashCode() {
        return contactId.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof Contact)) {
            return false;
        }
        
        Contact other = (Contact)o;
        return contactId.equals(other.getContactId());
    }
}
