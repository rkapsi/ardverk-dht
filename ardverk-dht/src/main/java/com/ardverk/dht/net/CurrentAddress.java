package com.ardverk.dht.net;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

import org.ardverk.collection.FixedSizeHashSet;
import org.ardverk.lang.NullArgumentException;
import org.ardverk.net.NetworkMask;

/**
 * 
 */
public class CurrentAddress {

    private final NetworkMask mask;
    
    // We use a ByteBuffer because it implements equals() and hashCode()
    private final FixedSizeHashSet<ByteBuffer> history;
    
    private InetAddress current = null;
    
    private InetAddress temporary = null;
    
    /**
     * 
     */
    public CurrentAddress(NetworkMask mask, int count) {
        this (null, mask, count);
    }
    
    /**
     * 
     */
    public CurrentAddress(InetAddress address, NetworkMask mask, int count) {
        if (mask == null) {
            throw new NullArgumentException("mask");
        }
        
        if (count < 0) {
            throw new IllegalArgumentException("count=" + count);
        }
        
        this.current = address;
        this.mask = mask;
        this.history = new FixedSizeHashSet<ByteBuffer>(count);
    }
    
    /**
     * 
     */
    public synchronized boolean set(SocketAddress src, SocketAddress address) {
        return set(((InetSocketAddress)src).getAddress(), 
                ((InetSocketAddress)address).getAddress());
    }
    
    /**
     * 
     */
    public synchronized boolean set(InetAddress src, InetAddress address) {
        if (src == null) {
            throw new NullArgumentException("src");
        }
        
        if (address == null) {
            throw new NullArgumentException("address");
        }
        
        // Do nothing if both addresses are equal
        if (current != null && current.equals(address)) {
            return true;
        }
        
        // Initialize the temporary address with the given value if it's null
        if (temporary == null) {
            temporary = address;
            
        // Reset the temporary address if it doesn't match with the given 
        // address and continue working with the one we already have
        } else if (!temporary.equals(address)) {
            temporary = null;
            history.clear();
            return false;
        }
        
        ByteBuffer network = ByteBuffer.wrap(mask.mask(src));
        
        // Make sure we're not accepting proposals more than once from the 
        // same Network during the discovery process
        if (!history.contains(network)) {
            history.add(network);
            if (history.isFull()) {
                current = temporary;
                temporary = null;
                history.clear();
            }
            
            return true;
        } 
        
        return false;
    }
    
    /**
     * Returns the current {@link InetAddress}
     */
    public synchronized InetAddress get() {
        return current;
    }
}
