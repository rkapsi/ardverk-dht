package com.ardverk.dht.message;

import java.net.SocketAddress;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.storage.Key;
import com.ardverk.dht.storage.ValueTuple;
import com.ardverk.dht.storage.Database.Condition;

/**
 * 
 */
public interface MessageFactory {
    
    /**
     * 
     */
    public MessageId createMessageId(SocketAddress dst);
    
    /**
     * 
     */
    public boolean isFor(MessageId messageId, SocketAddress src);
    
    /**
     * 
     */
    public PingRequest createPingRequest(SocketAddress dst);
    
    /**
     * 
     */
    public PingRequest createPingRequest(Contact dst);
    
    /**
     * 
     */
    public PingResponse createPingResponse(PingRequest request);
    
    /**
     * 
     */
    public NodeRequest createNodeRequest(Contact dst, KUID key);
    
    /**
     * 
     */
    public NodeResponse createNodeResponse(LookupRequest request, Contact[] contacts);
    
    /**
     * 
     */
    public ValueRequest createValueRequest(Contact dst, Key key);
    
    /**
     * 
     */
    public ValueResponse createValueResponse(LookupRequest request, ValueTuple tuple);
    
    /**
     * 
     */
    public StoreRequest createStoreRequest(Contact dst, ValueTuple tuple);
    
    /**
     * 
     */
    public StoreResponse createStoreResponse(StoreRequest request, Condition status);
}
