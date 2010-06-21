package com.ardverk.dht.message;

import com.ardverk.dht.KUID;

public interface NodeRequest extends LookupRequest {
    
    /**
     * 
     */
    public KUID getKey();
}
