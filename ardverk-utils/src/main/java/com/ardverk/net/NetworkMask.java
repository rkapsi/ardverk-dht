package com.ardverk.net;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;

import com.ardverk.io.Writable;
import com.ardverk.utils.ByteArrayComparator;

/**
 * A Network Mask
 */
public class NetworkMask implements Comparable<NetworkMask>, Serializable, Writable {
    
    private static final long serialVersionUID = 7628001660790804026L;
    
    private final byte[] mask;
    
    private final int hashCode;
    
    public NetworkMask(byte[] mask) {
        if (mask == null) {
            throw new NullPointerException("mask");
        }
        
        this.mask = mask.clone();
        this.hashCode = Arrays.hashCode(mask);
    }
    
    /**
     * Returns the byte mask
     */
    public byte[] getBytes() {
        return mask.clone();
    }
    
    public int length() {
        return mask.length;
    }
    
    @Override
    public int write(OutputStream out) throws IOException {
        out.write(mask);
        return length();
    }

    public byte[] mask(SocketAddress address) {
        return mask(((InetSocketAddress)address).getAddress());
    }
    
    public byte[] mask(InetAddress address) {
        return mask(address.getAddress(), false);
    }
    
    public byte[] mask(byte[] address) {
        return mask(address, true);
    }
    
    /**
     * Makes a copy of the given address (optional) and returns
     * a masked version of it.
     */
    byte[] mask(byte[] address, boolean copy) {
        if (address == null) {
            throw new NullPointerException("address");
        }
        
        if (copy) {
            address = address.clone();
        }
        
        int length = Math.min(address.length, mask.length);
        for (int i = 0; i < length; i++) {
            address[address.length - i - 1] &= mask[mask.length - i - 1]; 
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
        } else if (!(o instanceof NetworkMask)) {
            return false;
        }
        
        return compareTo((NetworkMask)o) == 0;
    }
    
    @Override
    public int compareTo(NetworkMask o) {
        return ByteArrayComparator.COMPARATOR.compare(mask, o.mask);
    }

    @Override
    public String toString() {
        return new BigInteger(1, mask).toString(16);
    }
}