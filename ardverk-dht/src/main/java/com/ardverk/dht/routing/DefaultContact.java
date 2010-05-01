package com.ardverk.dht.routing;

import java.net.SocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.ardverk.lang.NullArgumentException;

import com.ardverk.dht.KUID;

/**
 * 
 */
public class DefaultContact implements Contact {

    private final long creationTime;
    
    private final long timeStamp;
    
    private final Type type;
    
    private final KUID contactId;
    
    private final int instanceId;
    
    private final SocketAddress socketAddress;
    
    private final SocketAddress contactAddress;
    
    private Map<Object, Object> attributes;
    
    public DefaultContact(Type type, KUID contactId, 
            int instanceId, SocketAddress socketAddress, 
            SocketAddress contactAddress) {
        this(type, contactId, instanceId, socketAddress, 
                contactAddress, null);
    }
    
    public DefaultContact(Type type, KUID contactId, int instanceId, 
            SocketAddress socketAddress, SocketAddress contactAddress,
            Map<?, ?> attributes) {
        
        if (type == null) {
            throw new NullArgumentException("type");
        }
        
        if (contactId == null) {
            throw new NullArgumentException("contactId");
        }
        
        if (socketAddress == null) {
            throw new NullArgumentException("socketAddress");
        }
        
        if (contactAddress == null) {
            contactAddress = socketAddress;
        }
        
        this.type = type;
        this.creationTime = System.currentTimeMillis();
        this.timeStamp = creationTime;
        
        this.contactId = contactId;
        this.instanceId = instanceId;
        this.socketAddress = socketAddress;
        this.contactAddress = contactAddress;
        
        if (attributes != null && !attributes.isEmpty()) {
            this.attributes = new HashMap<Object, Object>(attributes);
        }
    }
    
    public DefaultContact(Contact existing, Contact contact) {
        
        if (existing == null) {
            throw new NullArgumentException("existing");
        }
        
        if (contact == null) {
            throw new NullArgumentException("contact");
        }
        
        if (!existing.getContactId().equals(contact.getContactId())) {
            throw new IllegalArgumentException();
        }
        
        // 2nd argument must be older
        if (contact.getCreationTime() < existing.getCreationTime()) {
            throw new IllegalArgumentException();
        }
        
        this.creationTime = existing.getCreationTime();
        this.timeStamp = contact.getTimeStamp();
        
        this.contactId = existing.getContactId();
        this.instanceId = contact.getInstanceId();
        this.socketAddress = contact.getSocketAddress();
        this.contactAddress = contact.getContactAddress();
        this.type = contact.getType();
        
        copyAttributes(existing, contact);
    }
    
    private DefaultContact(Contact contact, Type type) {
        if (contact == null) {
            throw new NullArgumentException("contact");
        }
        
        if (type == null) {
            throw new NullArgumentException("type");
        }
        
        this.creationTime = contact.getCreationTime();
        this.timeStamp = contact.getTimeStamp();
        
        this.contactId = contact.getContactId();
        this.instanceId = contact.getInstanceId();
        this.socketAddress = contact.getSocketAddress();
        this.contactAddress = contact.getContactAddress();
        this.type = type;
        
        copyAttributes(contact);
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
    public SocketAddress getSocketAddress() {
        return socketAddress;
    }
    
    @Override
    public SocketAddress getContactAddress() {
        return contactAddress;
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return contactAddress;
    }
    
    @Override
    public Type getType() {
        return type;
    }

    @Override
    public boolean isSolicited() {
        return type == Type.SOLICITED;
    }
    
    @Override
    public boolean isUnknown() {
        return type == Type.UNKNOWN;
    }

    @Override
    public boolean isUnsolicited() {
        return type == Type.UNSOLICITED;
    }

    @Override
    public boolean isActive() {
        return type.isActive();
    }

    @Override
    public synchronized Object getAttribute(Object key) {
        return attributes != null ? attributes.get(key) : null;
    }

    @Override
    public synchronized boolean hasAttribute(Object key) {
        return attributes != null ? attributes.containsKey(key) : false;
    }

    @Override
    public synchronized Object removeAttribute(Object key) {
        return attributes != null ? attributes.remove(key) : null;
    }

    @Override
    public synchronized Object setAttribute(Object key, Object value) {
        if (attributes == null) {
            attributes = new HashMap<Object, Object>();
        }
        return attributes.put(key, value);
    }

    @Override
    public synchronized Map<Object, Object> getAttributes() {
        return attributes != null ? attributes : Collections.emptyMap();
    }
    
    private synchronized void copyAttributes(Contact... src) {
        for (Contact contact : src) {
            synchronized (contact) {
                Map<Object, Object> attr = contact.getAttributes();
                if (attr != null && !attr.isEmpty()) {
                    if (attributes == null) {
                        attributes = new HashMap<Object, Object>(attr);
                    } else {
                        attributes.putAll(attr);
                    }
                }
            }
        }
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
    
    @Override
    public String toString() {
        return getContactId() + "(" + getInstanceId() 
            + ", " + getSocketAddress() + ", " + getRemoteAddress() + ")";
    }
}
