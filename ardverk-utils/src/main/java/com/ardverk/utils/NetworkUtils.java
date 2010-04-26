package com.ardverk.utils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

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
    
    /**
     * Returns true if the given port is valid.
     */
    public static boolean isValidPort(int port) {
        return 0 < port && port < 0xFFFF;
    }
    
    /**
     * Returns true if the given port is valid.
     */
    public static boolean isValidPort(SocketAddress address) {
        return isValidPort(((InetSocketAddress)address).getPort());
    }
}
