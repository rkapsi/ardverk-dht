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
public class NetworkMask implements Comparable<NetworkMask>, 
        Serializable, Writable, Cloneable {
    
    private static final long serialVersionUID = 7628001660790804026L;
    
    /**
     * A {@link NetworkMask} that does nothing
     */
    public static final NetworkMask NOP 
        = new NetworkMask(new byte[0]);
    
    /**
     * A {@link NetworkMask} for a Class-A network
     */
    public static final NetworkMask A 
        = new NetworkMask(new byte[] { 0x00, 0x00, 0x00 });
    
    /**
     * A {@link NetworkMask} for a Class-B network
     */
    public static final NetworkMask B 
        = new NetworkMask(new byte[] { 0x00, 0x00 });
    
    /**
     * A {@link NetworkMask} for a Class-C network
     */
    public static final NetworkMask C 
        = new NetworkMask(new byte[] { 0x00 });
    
    private final byte[] mask;
    
    private final int hashCode;
    
    /**
     * Creates a {@link NetworkMask}
     */
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

    /**
     * Returns the given {@link SocketAddress} as a mashed byte-array
     */
    public byte[] mask(SocketAddress address) {
        return mask(((InetSocketAddress)address).getAddress());
    }
    
    /**
     * Returns the given {@link InetAddress} as a mashed byte-array
     */
    public byte[] mask(InetAddress address) {
        return mask(address.getAddress(), false);
    }
    
    /**
     * Masks and returns the given byte-array
     */
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
    public NetworkMask clone() {
        return this;
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