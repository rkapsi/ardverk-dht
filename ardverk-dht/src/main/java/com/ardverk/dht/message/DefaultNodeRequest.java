package com.ardverk.dht.message;

import java.net.SocketAddress;

import org.ardverk.lang.Arguments;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact;

public class DefaultNodeRequest extends AbstractLookupRequest 
        implements NodeRequest {
    
    private final KUID lookupId;
    
    public DefaultNodeRequest(MessageId messageId, Contact contact, 
            SocketAddress address, KUID lookupId) {
        super(messageId, contact, address);
        
        this.lookupId = Arguments.notNull(lookupId, "lookupId");
    }

    @Override
    public KUID getKey() {
        return lookupId;
    }
}