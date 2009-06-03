package com.ardverk.net;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;

import com.ardverk.utils.ByteArrayComparator;

/**
 * A Network Mask
 */
public class Mask implements Comparable<Mask>, Serializable {
    
    private static final long serialVersionUID = 7628001660790804026L;
    
    /**
     * A Network mask that does nothing
     */
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
    
    /**
     * Returns the byte mask
     */
    public byte[] getBytes() {
        return mask.clone();
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