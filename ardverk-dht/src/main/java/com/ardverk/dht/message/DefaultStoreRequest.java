package com.ardverk.dht.message;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact2;

public class DefaultStoreRequest extends AbstractRequestMessage 
        implements StoreRequest {

    private final KUID key;
    
    private final byte[] value;
    
    public DefaultStoreRequest(MessageId messageId, 
            Contact2 contact, KUID key, byte[] value) {
        super(messageId, contact);
        
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