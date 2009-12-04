package com.ardverk.dht;

import java.net.InetAddress;
import java.net.SocketAddress;

import org.ardverk.concurrent.AsyncFuture;

import com.ardverk.dht.entity.GetEntity;
import com.ardverk.dht.entity.LookupEntity;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.entity.StoreEntity;
import com.ardverk.dht.routing.Contact;

/**
 * 
 */
interface DHT {

    /**
     * 
     */
    public Contact getContact(KUID contactId);
    
    /**
     * Sends a ping to the given host.
     */
    public AsyncFuture<PingEntity> ping(String address, int port);
    
    /**
     * Sends a ping to the given host.
     */
    public AsyncFuture<PingEntity> ping(InetAddress address, int port);

    /**
     * Sends a ping to the given host.
     */
    public AsyncFuture<PingEntity> ping(SocketAddress dst);
    
    /**
     * Sends a ping to the given host.
     */
    public AsyncFuture<PingEntity> ping(Contact contact);
    
    /**
     * 
     */
    public AsyncFuture<StoreEntity> put(KUID key, byte[] value);
    
    /**
     * 
     */
    public AsyncFuture<GetEntity> get(KUID key);
    
    /**
     * 
     */
    public AsyncFuture<LookupEntity> lookup(KUID key);
}
