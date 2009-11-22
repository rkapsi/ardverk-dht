package com.ardverk.dht;

import java.io.Closeable;

class Node implements Closeable {

    private final KUID nodeId;
    
    public Node(KUID nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public void close() {
        
    }
    
    public KUID getNodeId() {
        return nodeId;
    }
}
