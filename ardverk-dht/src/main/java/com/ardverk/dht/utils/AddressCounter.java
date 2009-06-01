package com.ardverk.dht.utils;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.ardverk.utils.ByteArrayComparator;

/**
 * A counter that counts Addresses or rather Networks.
 */
public class AddressCounter implements Serializable {
    
    private static final long serialVersionUID = -7103271018736085248L;

    private final byte[] mask;
    
    private final Map<byte[], AtomicInteger> map 
        = new TreeMap<byte[], AtomicInteger>(
            ByteArrayComparator.COMPARATOR);
    
    public AddressCounter(byte[] mask) {
        if (mask == null) {
            throw new NullPointerException("mask");
        }
        
        this.mask = mask;
    }
    
    public int add(SocketAddress address) {
        return add(((InetSocketAddress)address).getAddress());
    }
    
    public int add(InetAddress address) {
        return add(address.getAddress());
    }
    
    public synchronized int add(byte[] address) {
        byte[] key = mask(address, mask);
        AtomicInteger value = map.get(key);
        if (value == null) {
            value = new AtomicInteger();
            map.put(key, value);
        }
        
        return value.incrementAndGet();
    }
    
    public int remove(SocketAddress address) {
        return remove(((InetSocketAddress)address).getAddress());
    }
    
    public int remove(InetAddress address) {
        return remove(address.getAddress());
    }
    
    public synchronized int remove(byte[] address) {
        byte[] key = mask(address, mask);
        AtomicInteger value = map.get(key);
        if (value != null && value.decrementAndGet() <= 0) {
            map.remove(key);
        }
        return 0;
    }
    
    public int get(SocketAddress address) {
        return get(((InetSocketAddress)address).getAddress());
    }
    
    public int get(InetAddress address) {
        return get(address.getAddress());
    }
    
    public synchronized int get(byte[] address) {
        byte[] key = mask(address, mask);
        AtomicInteger value = map.get(key);
        if (value != null) {
            return value.get();
        }
        return 0;
    }
    
    public synchronized int size() {
        return map.size();
    }
    
    public synchronized boolean isEmpty() {
        return map.isEmpty();
    }
    
    public synchronized void clear() {
        map.clear();
    }
    
    private static byte[] mask(byte[] address, byte[] mask) {
        if (address == null) {
            throw new NullPointerException("address");
        }
        
        if (address.length != mask.length) {
            throw new IllegalArgumentException();
        }
        
        for (int i = 0; i < address.length; i++) {
            address[i] &= mask[i]; 
        }
        
        return address;
    }
}
