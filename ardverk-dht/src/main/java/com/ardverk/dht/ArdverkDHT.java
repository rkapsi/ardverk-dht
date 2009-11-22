package com.ardverk.dht;

import com.ardverk.dht.io.transport.Transport;

public class ArdverkDHT {

    private final Transport transport;
    
    public ArdverkDHT(Transport transport) {
        if (transport == null) {
            throw new NullPointerException("transport");
        }
        
        this.transport = transport;
    }
    
    public Transport getTransport() {
        return transport;
    }
}
