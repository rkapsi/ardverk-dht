package com.ardverk.dht.message;

import java.net.SocketAddress;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact2;
import com.ardverk.dht.storage.Database.Condition;

public interface MessageFactory {
    
    public MessageId createMessageId(SocketAddress dst);
    
    public boolean isFor(MessageId messageId, SocketAddress src);
    
    public PingRequest createPingRequest(SocketAddress dst);
    
    public PingRequest createPingRequest(Contact2 dst);
    
    public PingResponse createPingResponse(PingRequest request);
    
    public NodeRequest createNodeRequest(Contact2 dst, KUID key);
    
    public NodeResponse createNodeResponse(LookupRequest request, Contact2[] contacts);
    
    public ValueRequest createValueRequest(Contact2 dst, KUID key);
    
    public ValueResponse createValueResponse(LookupRequest request, byte[] value);
    
    public StoreRequest createStoreRequest(Contact2 dst, KUID key, byte[] value);
    
    public StoreResponse createStoreResponse(StoreRequest request, Condition status);
}
