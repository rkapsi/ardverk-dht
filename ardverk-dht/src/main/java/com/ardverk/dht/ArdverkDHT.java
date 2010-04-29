package com.ardverk.dht;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncProcess;

import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.entity.StoreEntity;
import com.ardverk.dht.entity.ValueEntity;
import com.ardverk.dht.io.DefaultMessageDispatcher;
import com.ardverk.dht.io.MessageDispatcher;
import com.ardverk.dht.io.NodeResponseHandler;
import com.ardverk.dht.io.PingResponseHandler;
import com.ardverk.dht.io.StoreResponseHandler;
import com.ardverk.dht.io.mina.MinaTransport;
import com.ardverk.dht.io.transport.Transport;
import com.ardverk.dht.message.BencodeMessageCodec;
import com.ardverk.dht.message.DefaultMessageFactory;
import com.ardverk.dht.message.MessageCodec;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.routing.Contact2;
import com.ardverk.dht.routing.DefaultRouteTable;
import com.ardverk.dht.routing.RouteTable;
import com.ardverk.dht.routing.Contact2.Type;
import com.ardverk.dht.storage.Database;
import com.ardverk.dht.storage.DefaultDatabase;

public class ArdverkDHT extends AbstractDHT implements Closeable {

    private static final int K = 20;
    
    private static final int KEY_SIZE = 20;
    
    private static final int MESSAGE_ID_SIZE = 20;
    
    private static final KeyFactory KEY_FACTORY 
        = new DefaultKeyFactory(KEY_SIZE);
    
    private static final MessageCodec CODEC 
        = new BencodeMessageCodec();
    
    private final RequestManager requestManager = new RequestManager();
    
    private final KUID contactId = KEY_FACTORY.createRandomKey();
    
    private final Transport transport;

    private final RouteTable routeTable;
    
    private final Database database;
    
    private final MessageFactory messageFactory;
    
    private final MessageDispatcher messageDispatcher;
    
    public ArdverkDHT(int port) throws IOException {
        ContactPinger pinger = new ContactPinger() {
            @Override
            public AsyncFuture<PingEntity> ping(Contact2 contact, 
                    long timeout, TimeUnit unit) {
                return ArdverkDHT.this.ping(contact, timeout, unit);
            }
        };
        
        transport = new MinaTransport(new InetSocketAddress(port));
        
        SocketAddress address = new InetSocketAddress("localhost", port);
        Contact2 localhost = new Contact2(Type.SOLICITED, 
                contactId, 0, address);
        
        routeTable = new DefaultRouteTable(pinger, K, localhost);
        
        database = new DefaultDatabase();
        
        Contact2 contact = routeTable.getLocalhost();
        messageFactory = new DefaultMessageFactory(
                MESSAGE_ID_SIZE, contact);
        
        messageDispatcher = new DefaultMessageDispatcher(
                transport, messageFactory, CODEC, 
                routeTable, database);
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
    public Transport getTransport() {
        return transport;
    }

    @Override
    public void close() {
        if (requestManager != null) {
            requestManager.close();
        }
    }

    @Override
    public Contact2 getContact(KUID contactId) {
        return routeTable.get(contactId);
    }
    
    @Override
    public ArdverkFuture<PingEntity> ping(Contact2 contact, 
            long timeout, TimeUnit unit) {
        AsyncProcess<PingEntity> process 
            = new PingResponseHandler(messageDispatcher, contact);
        return requestManager.submit(process, timeout, unit);
    }

    @Override
    public ArdverkFuture<PingEntity> ping(InetAddress address, int port, 
            long timeout, TimeUnit unit) {
        AsyncProcess<PingEntity> process 
            = new PingResponseHandler(messageDispatcher, address, port);
        return requestManager.submit(process, timeout, unit);
    }

    @Override
    public ArdverkFuture<PingEntity> ping(SocketAddress dst, 
            long timeout, TimeUnit unit) {
        AsyncProcess<PingEntity> process 
            = new PingResponseHandler(messageDispatcher, dst);
        return requestManager.submit(process, timeout, unit);
    }

    @Override
    public ArdverkFuture<PingEntity> ping(String address, int port, 
            long timeout, TimeUnit unit) {
        AsyncProcess<PingEntity> process 
            = new PingResponseHandler(messageDispatcher, address, port);
        return requestManager.submit(process, timeout, unit);
    }

    @Override
    public ArdverkFuture<StoreEntity> put(KUID key, byte[] value, 
            long timeout, TimeUnit unit) {
        AsyncProcess<StoreEntity> process 
            = new StoreResponseHandler(messageDispatcher, null, key, value);
        return requestManager.submit(process, timeout, unit);
    }
    
    @Override
    public ArdverkFuture<ValueEntity> get(KUID key, 
            long timeout, TimeUnit unit) {
        AsyncProcess<ValueEntity> process = null;
        return requestManager.submit(process, timeout, unit);
    }

    @Override
    public ArdverkFuture<NodeEntity> lookup(KUID key, 
            long timeout, TimeUnit unit) {
        AsyncProcess<NodeEntity> process 
            = new NodeResponseHandler(messageDispatcher, routeTable, key);
        return requestManager.submit(process, timeout, unit);
    }
}
