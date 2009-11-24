package com.ardverk.dht;

import java.io.Closeable;
import java.net.SocketAddress;

import org.ardverk.concurrent.AsyncFuture;

import com.ardverk.dht.message.Message;
import com.ardverk.dht.message.PingResponse;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.DefaultRouteTable;
import com.ardverk.dht.routing.RouteTable;

public class Node implements Closeable {

    private final RouteTable routeTable;
    
    private final ArdverkDHT dht;
    
    private final KUID nodeId;
    
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

    @Override
    public void close() {
        
    }
    
    public KUID getNodeId() {
        return nodeId;
    }
    
    public boolean isReady() {
        return false;
    }
    
    private AsyncFuture<PingResponse> ping(Contact contact) {
        return null;
    }

    public void handleMessage(SocketAddress src, Message message) {
        
    }
}
