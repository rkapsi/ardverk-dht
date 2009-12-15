package com.ardverk.dht.message;

import java.net.SocketAddress;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact;

public class DefaultNodeRequest extends AbstractRequestMessage 
        implements NodeRequest {

    private final KUID key;
    
    public DefaultNodeRequest(MessageId messageId, 
            Contact contact, Contact destination, KUID key) {
        super(messageId, contact, destination);
        
        if (key == null) {
            throw new NullPointerException("key");
        }
        
        this.key = key;
    }

    public DefaultNodeRequest(MessageId messageId, 
            Contact contact, SocketAddress address, KUID key) {
        super(messageId, contact, address);
        
        if (key == null) {
            throw new NullPointerException("key");
        }
        
        this.key = key;
    }

    @Override
    public KUID getKey() {
        return key;
    }
}