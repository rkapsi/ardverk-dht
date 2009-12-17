package com.ardverk.dht.message;

import com.ardverk.dht.KUID;

public interface LookupRequest extends RequestMessage {

    public KUID getKey();
}
