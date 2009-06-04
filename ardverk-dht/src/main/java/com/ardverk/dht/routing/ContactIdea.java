package com.ardverk.dht.routing;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact.State;

public class ContactIdea {
    
    private final long creationTime;
    
    private final long timeStamp;
    
    private final KUID contactId;
    
    private final int instanceId;
    
    private final SocketAddress address;
    
    private final State state;
    
    private final Map<Object, Object> attributes 
        = new ConcurrentHashMap<Object, Object>();
    
    public static ContactIdea createAlive(KUID contactId, int instanceId, 
            SocketAddress address, Map<?, ?> attributes) {
        
        return new ContactIdea(contactId, instanceId, 
                address, State.ALIVE, attributes);
    }
    
    public static ContactIdea createUnknown(KUID contactId, int instanceId, 
            SocketAddress address, Map<?, ?> attributes) {
        
        return new ContactIdea(contactId, instanceId, 
                address, State.UNKNOWN, attributes);
    }
    
    static ContactIdea merge(ContactIdea existing, ContactIdea contact) {
        return new ContactIdea(existing, contact);
    }
    
    private ContactIdea(KUID contactId, int instanceId, 
            SocketAddress address, State state, 
            Map<?, ?> attributes) {
        
        if (contactId == null) {
            throw new NullPointerException("contactId");
        }
        
        if (address == null) {
            throw new NullPointerException("address");
        }
        
        if (state == null) {
            throw new NullPointerException("state");
        }
        
        this.creationTime = System.currentTimeMillis();
        this.timeStamp = creationTime;
        
        this.contactId = contactId;
        this.instanceId = instanceId;
        this.address = address;
        this.state = state;
        
        if (attributes != null) {
            this.attributes.putAll(attributes);
        }
    }
    
    private ContactIdea(ContactIdea existing, ContactIdea contact) {
        
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
        
        this.state = (contact.getState() == State.ALIVE) 
                        ? State.ALIVE : existing.getState();
        
        this.attributes.putAll(existing.getAttributes());
        this.attributes.putAll(contact.getAttributes());
    }
    
    private ContactIdea(ContactIdea contact, State state) {
        if (contact == null) {
            throw new NullPointerException("contact");
        }
        
        if (state == null) {
            throw new NullPointerException("state");
        }
        
        this.creationTime = contact.getCreationTime();
        this.timeStamp = contact.getTimeStamp();
        
        this.contactId = contact.getContactId();
        this.instanceId = contact.getInstanceId();
        this.address = contact.getRemoteAddress();
        this.state = state;
        
        this.attributes.putAll(contact.getAttributes());   
    }
    
    public long getCreationTime() {
        return creationTime;
    }
    
    public long getTimeStamp() {
        return timeStamp;
    }

    public KUID getContactId() {
        return contactId;
    }

    public int getInstanceId() {
        return instanceId;
    }

    public SocketAddress getRemoteAddress() {
        return address;
    }
    
    public State getState() {
        return state;
    }

    ContactIdea changeState(State state) {
        return new ContactIdea(this, state);
    }

    public Object getAttribute(Object key) {
        return attributes.get(key);
    }

    public boolean hasAttribute(Object key) {
        return attributes.containsKey(key);
    }

    public Object removeAttribute(Object key) {
        return attributes.remove(key);
    }

    public Object setAttribute(Object key, Object value) {
        return attributes.put(key, value);
    }

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
        } else if (!(o instanceof ContactIdea)) {
            return false;
        }
        
        ContactIdea other = (ContactIdea)o;
        return contactId.equals(other.getContactId());
    }
}
