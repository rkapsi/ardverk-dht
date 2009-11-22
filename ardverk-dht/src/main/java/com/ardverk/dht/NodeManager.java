package com.ardverk.dht;

import java.io.Closeable;

import org.ardverk.collection.PatriciaTrie;
import org.ardverk.collection.Trie;

import com.ardverk.utils.IoUtils;

class NodeManager implements Closeable {

    private final Trie<KUID, Node> nodes 
        = new PatriciaTrie<KUID, Node>(
            KUID.createKeyAnalyzer(160));
    
    @Override
    public void close() {
        for (Node node : nodes.values()) {
            IoUtils.close(node);
        }
    }
    
    public synchronized boolean add(KUID nodeId) {
        if (nodeId == null) {
            throw new NullPointerException("nodeId");
        }
        
        if (!nodes.containsKey(nodeId)) {
            nodes.put(nodeId, new Node(nodeId));
            return true;
        }
        return false;
    }
    
    public synchronized Node remove(KUID nodeId) {
        if (nodeId == null) {
            throw new NullPointerException("nodeId");
        }
        
        return nodes.remove(nodeId);
    }
    
    public synchronized Node get(KUID nodeId) {
        if (nodeId == null) {
            throw new NullPointerException("nodeId");
        }
        
        return nodes.get(nodeId);
    }
    
    public synchronized Node select(KUID nodeId) {
        if (nodeId == null) {
            throw new NullPointerException("nodeId");
        }
        
        return nodes.selectValue(nodeId);
    }
}
