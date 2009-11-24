package com.ardverk.dht;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;

import org.ardverk.concurrent.AsyncFuture;

import com.ardverk.dht.io.transport.Transport;
import com.ardverk.dht.io.transport.TransportListener;
import com.ardverk.dht.message.Message;
import com.ardverk.dht.message.MessageFactory;

public class ArdverkDHT extends AbstractDHT implements DHT, Closeable {

    private final Transport transport;

    private final KeyFactory keyFactory;
    
    private final MessageFactory messageFactory;

    private final NodeManager nodeManager 
        = new NodeManager(this);

    private final TransportListener transportListener
            = new TransportListener() {
        @Override
        public void received(SocketAddress src, Object message)
                throws IOException {
            Message msg = messageFactory.decode(src, message);
            handleMessage(src, msg);
        }
    };
    
    public ArdverkDHT(Transport transport, 
            KeyFactory keyFactory, 
            MessageFactory messageFactory, 
            KUID nodeId) {
        
        if (transport == null) {
            throw new NullPointerException("transport");
        }
        
        if (keyFactory == null) {
            throw new NullPointerException("keyFactory");
        }
        if (messageFactory == null) {
            throw new NullPointerException("messageFactory");
        }
        
        if (nodeId == null) {
            throw new NullPointerException("nodeId");
        }
        
        this.transport = transport;
        this.keyFactory = keyFactory;
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
    
    public KeyFactory getKeyFactory() {
        return keyFactory;
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

    @Override
    public AsyncFuture<Pong> ping(SocketAddress dst) {
        Node node = getRandomNode();
        return null;
    }
    
    @Override
    public AsyncFuture<Object> put(KUID key, byte[] value) {
        Node node = nodeManager.select(key);
        return null;
    }
    
    @Override
    public AsyncFuture<Object> get(KUID key) {
        Node node = nodeManager.select(key);
        return null;
    }
    
    @Override
    public AsyncFuture<Object> lookup(KUID key) {
        Node node = nodeManager.select(key);
        return null;
    }
    
    private Node getRandomNode() {
        KUID randomId = keyFactory.createRandomKey();
        return nodeManager.select(randomId);
    }
    
    private void handleMessage(SocketAddress src, 
            Message message) throws IOException {
        
    }
}
