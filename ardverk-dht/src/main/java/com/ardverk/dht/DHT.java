package com.ardverk.dht;

import java.net.InetAddress;
import java.net.SocketAddress;

import org.ardverk.concurrent.AsyncFuture;

import com.ardverk.dht.message.PingResponse;
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
    public AsyncFuture<PingResponse> ping(String address, int port);
    
    /**
     * Sends a ping to the given host.
     */
    public AsyncFuture<PingResponse> ping(InetAddress address, int port);

    /**
     * Sends a ping to the given host.
     */
    public AsyncFuture<PingResponse> ping(SocketAddress dst);
    
    /**
     * Sends a ping to the given host.
     */
    public AsyncFuture<PingResponse> ping(Contact contact);
    
    /**
     * 
     */
    public AsyncFuture<Object> put(KUID key, byte[] value);
    
    /**
     * 
     */
    public AsyncFuture<Object> get(KUID key);
    
    /**
     * 
     */
    public AsyncFuture<Object> lookup(KUID key);
}
