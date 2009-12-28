package com.ardverk.dht.message;

import com.ardverk.dht.storage.Database.Status;

public interface StoreResponse extends ResponseMessage {

    public Status getStatus();
}
