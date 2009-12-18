package com.ardverk.dht;

import java.net.InetAddress;
import java.net.SocketAddress;

import org.ardverk.concurrent.AsyncFuture;

import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.entity.StoreEntity;
import com.ardverk.dht.entity.ValueEntity;
import com.ardverk.dht.io.transport.Transport;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.RouteTable;
import com.ardverk.dht.storage.Database;

/**
 * 
 */
interface DHT {

    /**
     * 
     */
    public Transport getTransport();
    
    /**
     * 
     */
    public RouteTable getRouteTable();
    
    /**
     * 
     */
    public Database getDatabase();
    
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
    public AsyncFuture<ValueEntity> get(KUID key);
    
    /**
     * 
     */
    public AsyncFuture<NodeEntity> lookup(KUID key);
}
