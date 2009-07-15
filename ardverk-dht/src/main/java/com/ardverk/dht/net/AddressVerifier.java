package com.ardverk.dht.net;

import java.net.InetAddress;

import com.ardverk.net.NetworkCounter;
import com.ardverk.net.NetworkMask;

public class AddressVerifier {

    private final NetworkCounter counter;
    
    private final 
    private InetAddress address;
    
    public AddressVerifier(NetworkMask mask) {
        if (mask == null) {
            throw new NullPointerException("mask");
        }
        
        counter = new NetworkCounter(mask);
    }
    
    public synchronized void set(InetAddress foo, InetAddress address) {
        if (foo == null) {
            throw new NullPointerException("foo");
        }
        
        if (address == null) {
            throw new NullPointerException("address");
        }
    }
    
    public synchronized InetAddress get() {
        
    }
}
