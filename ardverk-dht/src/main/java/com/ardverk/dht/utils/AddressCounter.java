package com.ardverk.dht.utils;

import java.io.Serializable;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.ardverk.utils.ByteArrayComparator;

/**
 * A counter that counts Addresses or rather Networks.
 */
public class AddressCounter implements Serializable {
    
    private static final long serialVersionUID = -7103271018736085248L;

    private final Mask mask;
    
    private final Map<byte[], AtomicInteger> map 
        = new TreeMap<byte[], AtomicInteger>(
            ByteArrayComparator.COMPARATOR);
    
    public AddressCounter(Mask mask) {
        if (mask == null) {
            throw new NullPointerException("mask");
        }
        
        this.mask = mask;
    }
    
    public Mask getMask() {
        return mask;
    }
    
    public int add(SocketAddress address) {
        return add(((InetSocketAddress)address).getAddress());
    }
    
    public int add(InetAddress address) {
        return add(address.getAddress());
    }
    
    public synchronized int add(byte[] address) {
        byte[] key = mask.mask(address);
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
        byte[] key = mask.mask(address);
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
        byte[] key = mask.mask(address);
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
    
    /**
     * A Network Mask
     */
    public static class Mask implements Comparable<Mask>, Serializable {
        
        private static final long serialVersionUID = 7628001660790804026L;
        
        public static final Mask NOP = new Mask(new byte[0]);
        
        private final byte[] mask;
        
        private final int hashCode;
        
        public Mask(byte[] mask) {
            if (mask == null) {
                throw new NullPointerException("mask");
            }
            
            this.mask = mask.clone();
            this.hashCode = Arrays.hashCode(mask);
        }
        
        public byte[] getBytes() {
            return mask.clone();
        }
        
        private byte[] mask(byte[] address) {
            if (address == null) {
                throw new NullPointerException("address");
            }
            
            if (address.length != mask.length) {
                throw new IllegalArgumentException();
            }
            
            int length = Math.min(address.length, mask.length);
            for (int i = 0; i < length; i++) {
                address[i] &= mask[i]; 
            }
            
            return address;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof Mask)) {
                return false;
            }
            
            return compareTo((Mask)o) == 0;
        }
        
        @Override
        public int compareTo(Mask o) {
            return ByteArrayComparator.COMPARATOR.compare(mask, o.mask);
        }

        @Override
        public String toString() {
            return new BigInteger(1, mask).toString(16);
        }
    }
}
