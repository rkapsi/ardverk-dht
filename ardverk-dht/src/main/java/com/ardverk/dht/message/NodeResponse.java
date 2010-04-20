package com.ardverk.dht.message;

import com.ardverk.dht.routing.Contact2;

public interface NodeResponse extends LookupResponse {

    public Contact2[] getContacts();
}
