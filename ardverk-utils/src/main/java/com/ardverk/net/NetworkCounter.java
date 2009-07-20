package com.ardverk.net;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.ardverk.coding.CodingUtils;
import com.ardverk.utils.ByteArrayComparator;

/**
 * A counter that counts Addresses or rather Networks.
 */
public class NetworkCounter implements Serializable {
    
    private static final long serialVersionUID = -7103271018736085248L;

    private final NetworkMask mask;
    
    private final Map<byte[], AtomicInteger> map 
        = new TreeMap<byte[], AtomicInteger>(
            ByteArrayComparator.COMPARATOR);
    
    public NetworkCounter(NetworkMask mask) {
        if (mask == null) {
            throw new NullPointerException("mask");
        }
        
        this.mask = mask;
    }
    
    /**
     * Returns the {@link NetworkMask} this {@link NetworkCounter} is using
     */
    public NetworkMask getMask() {
        return mask;
    }
    
    /**
     * Adds the given address and returns the number of addresses
     * in the same Network
     */
    public int add(SocketAddress address) {
        return addKey(mask.mask(address));
    }
    
    /**
     * Adds the given address and returns the number of addresses
     * in the same Network
     */
    public int add(InetAddress address) {
        return addKey(mask.mask(address));
    }
    
    /**
     * Adds the given address and returns the number of addresses
     * in the same Network
     */
    public int add(byte[] address) {
        return addKey(mask.mask(address));
    }
    
    /**
     * Adds the given address and returns the number of addresses
     * in the same Network
     */
    private synchronized int addKey(byte[] key) {
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
        return removeKey(mask.mask(address));
    }
    
    /**
     * Removes the given address and returns the remaining number
     * of addresses in the same Network
     */
    public int remove(InetAddress address) {
        return removeKey(mask.mask(address));
    }
    
    /**
     * Removes the given address and returns the remaining number
     * of addresses in the same Network
     */
    public int remove(byte[] address) {
        return removeKey(mask.mask(address));
    }
    
    /**
     * Removes the given address and returns the remaining number
     * of addresses in the same Network
     */
    private synchronized int removeKey(byte[] key) {
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
        return getKey(mask.mask(address));
    }
    
    /**
     * Returns the number addresses that are in the same Network
     */
    public int get(InetAddress address) {
        return getKey(mask.mask(address));
    }
    
    /**
     * Returns the number addresses that are in the same Network
     */
    public int get(byte[] address) {
        return getKey(mask.mask(address));
    }
     
    /**
     * Returns the number addresses that are in the same Network
     */
    private synchronized int getKey(byte[] key) {
        AtomicInteger value = map.get(key);
        if (value != null) {
            return value.get();
        }
        return 0;
    }
    
    /**
     * 
     */
    public synchronized Set<byte[]> keySet() {
        return Collections.unmodifiableSet(map.keySet());
    }
    
    /**
     * 
     */
    public synchronized Collection<Integer> values() {
        Integer[] values = new Integer[map.size()];
        
        int index = 0;
        for (AtomicInteger value : map.values()) {
            values[index++] = value.intValue();
        }
        
        return Arrays.asList(values);
    }
    
    /**
     * 
     */
    public synchronized Set<Map.Entry<byte[], Integer>> entrySet() {
        Map<byte[], Integer> copy = new TreeMap<byte[], Integer>(
                ByteArrayComparator.COMPARATOR);
        
        for (Map.Entry<byte[], AtomicInteger> entry : map.entrySet()) {
            copy.put(entry.getKey(), entry.getValue().intValue());
        }
        
        return copy.entrySet();
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
    
    @Override
    public synchronized String toString() {
        StringBuilder buffer = new StringBuilder("[");
        
        if (!map.isEmpty()) {
            for (Map.Entry<byte[], AtomicInteger> entry : map.entrySet()) {
                buffer.append(CodingUtils.encodeBase16(entry.getKey()))
                    .append("=").append(entry.getValue()).append(", ");
            }
            
            buffer.setLength(buffer.length()-2);
        }
        
        return buffer.append("]").toString();
    }
}
