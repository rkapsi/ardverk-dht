package com.ardverk.dht;

import java.io.Closeable;
import java.net.InetAddress;
import java.net.SocketAddress;

import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncProcess;

import com.ardverk.dht.entity.GetEntity;
import com.ardverk.dht.entity.LookupEntity;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.entity.StoreEntity;
import com.ardverk.dht.io.MessageDispatcher;
import com.ardverk.dht.io.PingResponseHandler;
import com.ardverk.dht.io.StoreResponseHandler;
import com.ardverk.dht.message.Message;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.DefaultRouteTable;
import com.ardverk.dht.routing.RouteTable;

public class Node implements DHT, Closeable {

    private final RequestManager requestManager = new RequestManager();
    
    private final MessageDispatcher messageDispatcher = null;
    
    private final RouteTable routeTable;
    
    private final ArdverkDHT dht;
    
    private final KUID nodeId;
    
    private boolean open = true;
    
    private boolean running = false;
    
    Node(ArdverkDHT dht, KUID nodeId) {
        if (dht == null) {
            throw new NullPointerException("dht");
        }
        
        if (nodeId == null) {
            throw new NullPointerException("nodeId");
        }
        
        this.dht = dht;
        this.nodeId = nodeId;
        
        ContactPinger pinger = new ContactPinger() {
            @Override
            public AsyncFuture<PingEntity> ping(Contact contact) {
                return Node.this.ping(contact);
            }
        };
        
        routeTable = new DefaultRouteTable(
                pinger, null, 20, nodeId, 0, null);
    }

    public synchronized boolean isOpen() {
        return open;
    }

    public synchronized boolean isRunning() {
        return open && running;
    }
    
    public synchronized void start() {
        if (!open) {
            throw new IllegalStateException();
        }
        
        if (running) {
            throw new IllegalStateException();
        }
        
        running = true;
        // TODO: Start the thing!
    }
    
    @Override
    public synchronized void close() {
        if (!open) {
            return;
        }
        
        open = false;
        
        if (requestManager != null) {
            requestManager.close();
        }
        
        if (running) {
            // TODO: Stop that thing!
        }
    }
    
    public KUID getNodeId() {
        return nodeId;
    }
    
    public boolean isReady() {
        return false;
    }
    
    public RouteTable getRouteTable() {
        return routeTable;
    }

    public void handleMessage(SocketAddress src, Message message) {
        
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
            = new StoreResponseHandler(messageDispatcher, key, value);
        return requestManager.submit(process);
    }
    
    @Override
    public AsyncFuture<GetEntity> get(KUID key) {
        AsyncProcess<GetEntity> process = null;
        return requestManager.submit(process);
    }

    @Override
    public AsyncFuture<LookupEntity> lookup(KUID key) {
        AsyncProcess<LookupEntity> process = null;
        return requestManager.submit(process);
    }
}
