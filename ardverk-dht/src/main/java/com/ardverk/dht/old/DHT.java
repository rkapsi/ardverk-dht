package com.ardverk.dht;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.entity.BootstrapEntity;
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.entity.StoreEntity;
import com.ardverk.dht.entity.ValueEntity;
import com.ardverk.dht.io.BootstrapProcess.Config;
import com.ardverk.dht.io.transport.Transport;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.RouteTable;
import com.ardverk.dht.storage.Database;


/**
 * 
 */
public interface DHT extends AddressPinger, ContactPinger, Closeable {

    /**
     * 
     */
    public void bind(Transport transport) throws IOException;
    
    /**
     * 
     */
    public Transport unbind();
    
    /**
     * 
     */
    public boolean isBound();
    
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
    public Contact getLocalhost();
    
    /**
     * 
     */
    public Contact getContact(KUID contactId);
    
    /**
     * 
     */
    public ArdverkFuture<BootstrapEntity> bootstrap(Config config, 
            long timeout, TimeUnit unit);
    
    /**
     * 
     */
    public ArdverkFuture<StoreEntity> put(KUID key, byte[] value, 
            long timeout, TimeUnit unit);
    
    /**
     * 
     */
    public ArdverkFuture<ValueEntity> get(KUID key, 
            long timeout, TimeUnit unit);
    
    /**
     * 
     */
    public ArdverkFuture<NodeEntity> lookup(KUID key, 
            long timeout, TimeUnit unit);
    
    /**
     * 
     */
    /*public <T> ArdverkFuture<T> submit(AsyncProcess<T> process, 
            long timeout, TimeUnit unit);*/
}
