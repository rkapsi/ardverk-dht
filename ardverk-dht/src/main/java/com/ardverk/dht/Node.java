package com.ardverk.dht;

import java.io.Closeable;
import java.net.SocketAddress;

import com.ardverk.dht.message.Message;
import com.ardverk.dht.routing.DefaultRouteTable;
import com.ardverk.dht.routing.RouteTable;

class Node implements Closeable {

    private final RouteTable routeTable 
        = new DefaultRouteTable(pinger, contactFactory, 
                k, contactId, instanceId, address);
    
    private final KUID nodeId;
    
    public Node(KUID nodeId) {
        if (nodeId == null) {
            throw new NullPointerException("nodeId");
        }
        
        this.nodeId = nodeId;
    }

    @Override
    public void close() {
        
    }
    
    public KUID getNodeId() {
        return nodeId;
    }
    
    public void handleMessage(SocketAddress src, Message message) {
        
    }
}
