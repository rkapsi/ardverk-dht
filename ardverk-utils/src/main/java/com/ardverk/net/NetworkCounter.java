/*
 * Copyright 2010 Roger Kapsi
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.ardverk.net;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.NullArgumentException;

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
    
    /**
     * Creates a {@link NetworkCounter} with the given {@link NetworkMask}.
     */
    public NetworkCounter(NetworkMask mask) {
        if (mask == null) {
            throw new NullArgumentException("mask");
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
     * Returns a {@link Set} view of the keys contained 
     * in this {@link NetworkCounter}.
     */
    public synchronized Set<byte[]> keySet() {
        return new TreeSet<byte[]>((SortedSet<byte[]>)map.keySet());
    }
    
    /**
     * Returns a {@link Collection} view of the values contained 
     * in this {@link NetworkCounter}.
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
     * Returns a {@link Set} view of the mappings contained 
     * in this {@link NetworkCounter}.
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
