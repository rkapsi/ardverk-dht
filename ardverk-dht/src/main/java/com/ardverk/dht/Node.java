package com.ardverk.dht;

import java.io.Closeable;
import java.net.InetAddress;
import java.net.SocketAddress;

import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncProcess;

import com.ardverk.dht.io.MessageDispatcher;
import com.ardverk.dht.io.process.PingProcess;
import com.ardverk.dht.message.Message;
import com.ardverk.dht.message.PingResponse;
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
            public AsyncFuture<PingResponse> ping(Contact contact) {
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
    public AsyncFuture<Object> get(KUID key) {
        AsyncProcess<Object> process = null;
        return requestManager.submit(process);
    }

    @Override
    public AsyncFuture<Object> lookup(KUID key) {
        AsyncProcess<Object> process = null;
        return requestManager.submit(process);
    }

    @Override
    public AsyncFuture<PingResponse> ping(Contact contact) {
        AsyncProcess<PingResponse> process 
            = new PingProcess(messageDispatcher);
        return requestManager.submit(process);
    }

    @Override
    public AsyncFuture<PingResponse> ping(InetAddress address, int port) {
        AsyncProcess<PingResponse> process 
            = new PingProcess(messageDispatcher);
        return requestManager.submit(process);
    }

    @Override
    public AsyncFuture<PingResponse> ping(SocketAddress dst) {
        AsyncProcess<PingResponse> process 
            = new PingProcess(messageDispatcher);
        return requestManager.submit(process);
    }

    @Override
    public AsyncFuture<PingResponse> ping(String address, int port) {
        AsyncProcess<PingResponse> process 
            = new PingProcess(messageDispatcher);
        return requestManager.submit(process);
    }

    @Override
    public AsyncFuture<Object> put(KUID key, byte[] value) {
        AsyncProcess<Object> process = null;
        return requestManager.submit(process);
    }
}
