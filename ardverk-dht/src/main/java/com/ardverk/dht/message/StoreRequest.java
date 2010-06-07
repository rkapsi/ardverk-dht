package com.ardverk.dht.message;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact;

/**
 * 
 */
public interface StoreRequest extends RequestMessage {

    /**
     * 
     */
    public int getId();
    
    /**
     * 
     */
    public Contact getCreator();
    
    /**
     * 
     */
    public KUID getKey();
    
    /**
     * 
     */
    public byte[] getValue();
}
