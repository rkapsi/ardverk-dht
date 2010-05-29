package com.ardverk.dht;

import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.entity.PingEntity;

/**
 * 
 */
public interface AddressPinger {

    /**
     * Sends a ping to the given host.
     */
    public ArdverkFuture<PingEntity> ping(
            String address, int port, long timeout, TimeUnit unit);
    
    /**
     * Sends a ping to the given host.
     */
    public ArdverkFuture<PingEntity> ping(
            InetAddress address, int port, long timeout, TimeUnit unit);
    
    /**
     * Sends a ping to the given host.
     */
    public ArdverkFuture<PingEntity> ping(
            SocketAddress address, long timeout, TimeUnit unit);
}
