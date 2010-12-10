package com.ardverk.dht.routing;

import java.net.SocketAddress;

import com.ardverk.dht.KUID;

/**
 * A callback interface for the {@link RouteTable}.
 */
interface IoErrorCallback {
    
    /**
     * A callback that is called if an I/O error occurred for the given
     * {@link KUID} and {@link SocketAddress} pair.
     */
    public void handleIoError(KUID contactId, SocketAddress address);
}