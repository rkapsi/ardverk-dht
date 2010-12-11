package com.ardverk.dht.utils;

import java.net.SocketAddress;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact;

/**
 * The {@link ContactKey} can be used to keep track of certain operations
 * such as sending out PING requests.
 */
public class ContactKey {
    
    private final KUID contactId;
    
    private final SocketAddress address;
    
    public ContactKey(Contact contact) {
        this(contact.getId(), contact.getRemoteAddress());
    }
    
    public ContactKey(KUID contactId, SocketAddress address) {
        this.contactId = contactId;
        this.address = address;
    }
    
    @Override
    public int hashCode() {
        return 31*contactId.hashCode() + address.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof ContactKey)) {
            return false;
        }
        
        ContactKey other = (ContactKey)o;
        return contactId.equals(other.contactId) 
                && address.equals(other.address);
    }
    
    @Override
    public String toString() {
        return contactId + "/" + address;
    }
}