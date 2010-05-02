package com.ardverk.dht;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncProcess;

import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.entity.BootstrapEntity;
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.entity.StoreEntity;
import com.ardverk.dht.entity.ValueEntity;
import com.ardverk.dht.io.BootstrapProcess;
import com.ardverk.dht.io.DefaultMessageDispatcher;
import com.ardverk.dht.io.MessageDispatcher;
import com.ardverk.dht.io.NodeResponseHandler;
import com.ardverk.dht.io.PingResponseHandler;
import com.ardverk.dht.io.StoreResponseHandler;
import com.ardverk.dht.io.BootstrapProcess.Config;
import com.ardverk.dht.io.transport.Transport;
import com.ardverk.dht.message.MessageCodec;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.routing.Contact2;
import com.ardverk.dht.routing.RouteTable;
import com.ardverk.dht.storage.Database;

public class ArdverkDHT extends AbstractDHT implements Closeable {
    
    private final RequestManager requestManager = new RequestManager();
    
    private final RouteTable routeTable;
    
    private final Database database;
    
    //private final MessageFactory messageFactory;
    
    private final MessageDispatcher messageDispatcher;
    
    public ArdverkDHT(MessageCodec codec, MessageFactory messageFactory, 
            RouteTable routeTable, Database database) {
        
        //this.messageFactory = messageFactory;
        this.routeTable = routeTable;
        this.database = database;
        
        messageDispatcher = new DefaultMessageDispatcher(
                messageFactory, codec, routeTable, database);
    }
    
    @Override
    public boolean isBound() {
        return messageDispatcher.isBound();
    }
    
    @Override
    public void bind(Transport transport) throws IOException {
        routeTable.bind(this);
        messageDispatcher.bind(transport);
    }

    @Override
    public Transport unbind() {
        routeTable.unbind();
        return messageDispatcher.unbind();
    }

    @Override
    public Transport getTransport() {
        return messageDispatcher.getTransport();
    }
    
    @Override
    public Database getDatabase() {
        return database;
    }
    
    @Override
    public RouteTable getRouteTable() {
        return routeTable;
    }
    
    @Override
    public void close() {
        if (requestManager != null) {
            requestManager.close();
        }
        
        messageDispatcher.close();
    }
    
    @Override
    public Contact2 getLocalhost() {
        return routeTable.getLocalhost();
    }

    @Override
    public Contact2 getContact(KUID contactId) {
        return routeTable.get(contactId);
    }
    
    @Override
    public ArdverkFuture<BootstrapEntity> bootstrap(Config config,
            long timeout, TimeUnit unit) {
        
        BootstrapProcess process = new BootstrapProcess(
                this, config, timeout, unit);
        
        return submit(process, timeout, unit);
    }

    @Override
    public ArdverkFuture<PingEntity> ping(Contact2 contact, 
            long timeout, TimeUnit unit) {
        AsyncProcess<PingEntity> process 
            = new PingResponseHandler(messageDispatcher, contact);
        return submit(process, timeout, unit);
    }

    @Override
    public ArdverkFuture<PingEntity> ping(InetAddress address, int port, 
            long timeout, TimeUnit unit) {
        AsyncProcess<PingEntity> process 
            = new PingResponseHandler(messageDispatcher, address, port);
        return submit(process, timeout, unit);
    }

    @Override
    public ArdverkFuture<PingEntity> ping(SocketAddress dst, 
            long timeout, TimeUnit unit) {
        AsyncProcess<PingEntity> process 
            = new PingResponseHandler(messageDispatcher, dst);
        return submit(process, timeout, unit);
    }

    @Override
    public ArdverkFuture<PingEntity> ping(String address, int port, 
            long timeout, TimeUnit unit) {
        AsyncProcess<PingEntity> process 
            = new PingResponseHandler(messageDispatcher, address, port);
        return submit(process, timeout, unit);
    }

    @Override
    public ArdverkFuture<StoreEntity> put(KUID key, byte[] value, 
            long timeout, TimeUnit unit) {
        AsyncProcess<StoreEntity> process 
            = new StoreResponseHandler(messageDispatcher, null, key, value);
        return submit(process, timeout, unit);
    }
    
    @Override
    public ArdverkFuture<ValueEntity> get(KUID key, 
            long timeout, TimeUnit unit) {
        AsyncProcess<ValueEntity> process = null;
        return submit(process, timeout, unit);
    }

    @Override
    public ArdverkFuture<NodeEntity> lookup(KUID key, 
            long timeout, TimeUnit unit) {
        AsyncProcess<NodeEntity> process 
            = new NodeResponseHandler(messageDispatcher, routeTable, key);
        return submit(process, timeout, unit);
    }

    @Override
    public <T> ArdverkFuture<T> submit(AsyncProcess<T> process, long timeout,
            TimeUnit unit) {
        return requestManager.submit(process, timeout, unit);
    }
}
