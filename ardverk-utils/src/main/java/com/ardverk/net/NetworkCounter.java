package com.ardverk.net;

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
public class NetworkCounter implements Serializable {
    
    private static final long serialVersionUID = -7103271018736085248L;

    private final Mask mask;
    
    private final Map<byte[], AtomicInteger> map 
        = new TreeMap<byte[], AtomicInteger>(
            ByteArrayComparator.COMPARATOR);
    
    public NetworkCounter(Mask mask) {
        if (mask == null) {
            throw new NullPointerException("mask");
        }
        
        this.mask = mask;
    }
    
    /**
     * Returns the {@link Mask} this {@link NetworkCounter} is using
     */
    public Mask getMask() {
        return mask;
    }
    
    /**
     * Adds the given address and returns the number of addresses
     * in the same Network
     */
    public int add(SocketAddress address) {
        return add(((InetSocketAddress)address).getAddress());
    }
    
    /**
     * Adds the given address and returns the number of addresses
     * in the same Network
     */
    public int add(InetAddress address) {
        return add(address.getAddress(), false);
    }
    
    /**
     * Adds the given address and returns the number of addresses
     * in the same Network
     */
    public int add(byte[] address) {
        return add(address, true);
    }
    
    /**
     * Adds the given address and returns the number of addresses
     * in the same Network
     */
    private synchronized int add(byte[] address, boolean copy) {
        byte[] key = mask.mask(address, copy);
        AtomicInteger value = map.get(key);
        if (value == null) {
            value = new AtomicInteger();
            map.put(key, value);
        }
        
        return value.incrementAndGet();
    }
    
    /**
     * Removes the given address and returns the remaining number
     * of addresses in the same Network
     */
    public int remove(SocketAddress address) {
        return remove(((InetSocketAddress)address).getAddress());
    }
    
    /**
     * Removes the given address and returns the remaining number
     * of addresses in the same Network
     */
    public int remove(InetAddress address) {
        return remove(address.getAddress(), false);
    }
    
    /**
     * Removes the given address and returns the remaining number
     * of addresses in the same Network
     */
    public int remove(byte[] address) {
        return remove(address, true);
    }
    
    /**
     * Removes the given address and returns the remaining number
     * of addresses in the same Network
     */
    private synchronized int remove(byte[] address, boolean copy) {
        byte[] key = mask.mask(address, copy);
        AtomicInteger value = map.get(key);
        if (value != null && value.decrementAndGet() <= 0) {
            map.remove(key);
            return 0;
        }
        return value.get();
    }
    
    /**
     * Returns the number addresses that are in the same Network
     */
    public int get(SocketAddress address) {
        return get(((InetSocketAddress)address).getAddress());
    }
    
    /**
     * Returns the number addresses that are in the same Network
     */
    public int get(InetAddress address) {
        return get(address.getAddress(), false);
    }
    
    /**
     * Returns the number addresses that are in the same Network
     */
    public int get(byte[] address) {
        return get(address, true);
    }
     
    /**
     * Returns the number addresses that are in the same Network
     */
    private synchronized int get(byte[] address, boolean copy) {
        byte[] key = mask.mask(address, copy);
        AtomicInteger value = map.get(key);
        if (value != null) {
            return value.get();
        }
        return 0;
    }
    
    /**
     * Returns the number of networks
     */
    public synchronized int size() {
        return map.size();
    }
    
    /**
     * Returns true if this {@link NetworkCounter} is empty
     */
    public synchronized boolean isEmpty() {
        return map.isEmpty();
    }
    
    /**
     * Clears the {@link NetworkCounter}
     */
    public synchronized void clear() {
        map.clear();
    }
}
