package com.ardverk.dht;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;

import com.ardverk.dht.io.transport.Transport;
import com.ardverk.dht.io.transport.TransportListener;
import com.ardverk.dht.message.Message;
import com.ardverk.dht.message.MessageFactory;

public class ArdverkDHT implements Closeable {

    private final MessageFactory messageFactory;
    
    private final TransportListener transportListener
            = new TransportListener() {
        @Override
        public void received(SocketAddress src, Object message)
                throws IOException {
            Message msg = messageFactory.decode(src, message);
            handleMessage(src, msg);
        }
    };
    
    private final NodeManager nodeManager 
        = new NodeManager(this);
    
    private final Transport transport;
    
    public ArdverkDHT(Transport transport, 
            MessageFactory messageFactory, KUID nodeId) {
        if (transport == null) {
            throw new NullPointerException("transport");
        }
        
        if (messageFactory == null) {
            throw new NullPointerException("messageFactory");
        }
        
        if (nodeId == null) {
            throw new NullPointerException("nodeId");
        }
        
        this.transport = transport;
        this.messageFactory = messageFactory;
        
        nodeManager.add(nodeId);
        transport.addTransportListener(transportListener);
    }
    
    @Override
    public void close() {
        transport.removeTransportListener(transportListener);
    }
    
    public Transport getTransport() {
        return transport;
    }
    
    public boolean addNode(KUID nodeId) {
        return nodeManager.add(nodeId);
    }
    
    public boolean removeNode(KUID nodeId) {
        Node node = nodeManager.remove(nodeId);
        if (node != null) {
            node.close();
            return true;
        }
        return false;
    }
    
    private void handleMessage(SocketAddress src, 
            Message message) throws IOException {
        
    }
}
