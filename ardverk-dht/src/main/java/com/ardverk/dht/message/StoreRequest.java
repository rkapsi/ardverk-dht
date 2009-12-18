package com.ardverk.dht.message;

import com.ardverk.dht.KUID;

public interface StoreRequest extends RequestMessage {

    public KUID getKey();
    
    public byte[] getValue();
}
