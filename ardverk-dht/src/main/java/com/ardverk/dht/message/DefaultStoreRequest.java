package com.ardverk.dht.message;

import java.net.SocketAddress;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact;

public class DefaultStoreRequest extends AbstractRequestMessage 
        implements StoreRequest {

    private final KUID key;
    
    private final byte[] value;
    
    public DefaultStoreRequest(MessageId messageId, 
            Contact contact, Contact destination, 
            KUID key, byte[] value) {
        super(messageId, contact, destination);
        
        if (key == null) {
            throw new NullPointerException("key");
        }
        
        if (value == null) {
            throw new NullPointerException("value");
        }
        
        this.key = key;
        this.value = value;
    }

    public DefaultStoreRequest(MessageId messageId, 
            Contact contact, SocketAddress address, 
            KUID key, byte[] value) {
        super(messageId, contact, address);
        
        if (key == null) {
            throw new NullPointerException("key");
        }
        
        if (value == null) {
            throw new NullPointerException("value");
        }
        
        this.key = key;
        this.value = value;
    }

    @Override
    public KUID getKey() {
        return key;
    }

    @Override
    public byte[] getValue() {
        return value;
    }
}