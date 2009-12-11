package com.ardverk.dht;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;

import org.ardverk.concurrent.AsyncFuture;

import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.entity.StoreEntity;
import com.ardverk.dht.entity.ValueEntity;
import com.ardverk.dht.io.transport.Transport;
import com.ardverk.dht.io.transport.TransportListener;
import com.ardverk.dht.message.Message;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.routing.Contact;

public class ArdverkDHT extends AbstractDHT implements DHT, Closeable {

    private final Transport transport;

    private final KeyFactory keyFactory;
    
    private final MessageFactory messageFactory;

    private final NodeManager nodeManager 
        = new NodeManager(this);

    private final TransportListener transportListener
            = new TransportListener() {
        @Override
        public void received(SocketAddress src, byte[] message)
                throws IOException {
            //Message msg = messageFactory.decode(src, message);
            //handleMessage(src, msg);
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
        nodeManager.close();
    }
    
    public Transport getTransport() {
        return transport;
    }
    
    public KeyFactory getKeyFactory() {
        return keyFactory;
    }
    
    public Node addNode(KUID nodeId) {
        return nodeManager.add(nodeId);
    }
    
    public Node removeNode(KUID nodeId) {
        return nodeManager.remove(nodeId);
    }
    
    public Node getNode(KUID nodeId) {
        return nodeManager.get(nodeId);
    }
    
    public Node[] getNodes() {
        return nodeManager.getNodes();
    }
    
    @Override
    public Contact getContact(KUID contactId) {
        for (Node node : nodeManager.getNodes()) {
            Contact contact = node.getContact(contactId);
            if (contact != null) {
                return contact;
            }
        }
        
        return null;
    }

    @Override
    public AsyncFuture<PingEntity> ping(SocketAddress dst) {
        return getRandomNode().ping(dst);
    }
    
    @Override
    public AsyncFuture<PingEntity> ping(Contact contact) {
        // TODO: Need reference to Node. I guess we could use
        // also a random Node to send the ping but it would be
        // overall better to send the ping from the Node that
        // is managing the given Contact.
        return null;
    }

    @Override
    public AsyncFuture<StoreEntity> put(KUID key, byte[] value) {
        return nodeManager.select(key).put(key, value);
    }
    
    @Override
    public AsyncFuture<ValueEntity> get(KUID key) {
        return nodeManager.select(key).get(key);
    }
    
    @Override
    public AsyncFuture<NodeEntity> lookup(KUID key) {
        return nodeManager.select(key).lookup(key);
    }
    
    private Node getRandomNode() {
        KUID randomId = keyFactory.createRandomKey();
        return nodeManager.select(randomId);
    }
    
    private void handleMessage(SocketAddress src, 
            Message message) throws IOException {
        
    }
}
