package com.ardverk.dht;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncProcess;

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
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.ContactFactory;
import com.ardverk.dht.routing.DefaultContactFactory;
import com.ardverk.dht.routing.DefaultRouteTable;
import com.ardverk.dht.routing.RouteTable;
import com.ardverk.dht.storage.Database;
import com.ardverk.dht.storage.DefaultDatabase;

public class ArdverkDHT extends AbstractDHT implements Closeable {

    private static final int K = 20;
    
    private static final int KEY_SIZE = 20;
    
    private static final int MESSAGE_ID_SIZE = 20;
    
    private static final KeyFactory KEY_FACTORY 
        = new DefaultKeyFactory(KEY_SIZE);
    
    private static final ContactFactory CONTACT_FACTORY 
        = new DefaultContactFactory(KEY_FACTORY);

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
            public AsyncFuture<PingEntity> ping(Contact contact) {
                return ArdverkDHT.this.ping(contact);
            }
        };
        
        transport = new MinaTransport(new InetSocketAddress(port));
        
        routeTable = new DefaultRouteTable(pinger, 
                CONTACT_FACTORY, K, contactId, 
                0, new InetSocketAddress("localhost", port));
        
        database = new DefaultDatabase();
        
        Contact contact = routeTable.getLocalhost();
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
    public Contact getContact(KUID contactId) {
        return routeTable.get(contactId);
    }
    
    @Override
    public AsyncFuture<PingEntity> ping(Contact contact) {
        AsyncProcess<PingEntity> process 
            = new PingResponseHandler(messageDispatcher, contact);
        return requestManager.submit(process);
    }

    @Override
    public AsyncFuture<PingEntity> ping(InetAddress address, int port) {
        AsyncProcess<PingEntity> process 
            = new PingResponseHandler(messageDispatcher, address, port);
        return requestManager.submit(process);
    }

    @Override
    public AsyncFuture<PingEntity> ping(SocketAddress dst) {
        AsyncProcess<PingEntity> process 
            = new PingResponseHandler(messageDispatcher, dst);
        return requestManager.submit(process);
    }

    @Override
    public AsyncFuture<PingEntity> ping(String address, int port) {
        AsyncProcess<PingEntity> process 
            = new PingResponseHandler(messageDispatcher, address, port);
        return requestManager.submit(process);
    }

    @Override
    public AsyncFuture<StoreEntity> put(KUID key, byte[] value) {
        AsyncProcess<StoreEntity> process 
            = new StoreResponseHandler(messageDispatcher, null, key, value);
        return requestManager.submit(process);
    }
    
    @Override
    public AsyncFuture<ValueEntity> get(KUID key) {
        AsyncProcess<ValueEntity> process = null;
        return requestManager.submit(process);
    }

    @Override
    public AsyncFuture<NodeEntity> lookup(KUID key) {
        AsyncProcess<NodeEntity> process 
            = new NodeResponseHandler(messageDispatcher, routeTable, key);
        return requestManager.submit(process);
    }
}
