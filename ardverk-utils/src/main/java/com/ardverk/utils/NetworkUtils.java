package com.ardverk.utils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;

import com.ardverk.net.NetworkMask;

public class NetworkUtils {

    private NetworkUtils() {}
    
    /**
     * Returns true if both {@link SocketAddress}es have the same {@link InetAddress}
     */
    public static boolean isSameAddress(SocketAddress a1, SocketAddress a2) {
        if (a1 == null) {
            throw new NullPointerException("address1");
        }
        
        if (a2 == null) {
            throw new NullPointerException("address2");
        }
        
        return ((InetSocketAddress)a1).getAddress().equals(
                ((InetSocketAddress)a2).getAddress());
    }
    
    public static boolean isSameNetwork(SocketAddress a1, 
            SocketAddress a2, NetworkMask mask) {
        
        if (a1 == null) {
            throw new NullPointerException("address1");
        }
        
        if (a2 == null) {
            throw new NullPointerException("address2");
        }
        
        if (mask == null) {
            throw new NullPointerException("mask");
        }
        
        return Arrays.equals(mask.mask(a1), mask.mask(a2));
    }
}
