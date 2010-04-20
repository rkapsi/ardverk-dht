package com.ardverk.dht.message;

import java.net.SocketAddress;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact2;
import com.ardverk.dht.storage.Database.Condition;

public class DefaultMessageFactory extends AbstractMessageFactory {

    private final Contact2 contact;
    
    public DefaultMessageFactory(int length, Contact2 contact) {
        super(length);
        
        if (contact == null) {
            throw new NullPointerException("contact");
        }
        
        this.contact = contact;
    }
    
    @Override
    public PingRequest createPingRequest(Contact2 dst) {
        return createPingRequest(dst.getRemoteAddress());
    }

    @Override
    public PingRequest createPingRequest(SocketAddress dst) {
        MessageId messageId = createMessageId(dst);
        return new DefaultPingRequest(messageId, contact, dst);
    }

    @Override
    public PingResponse createPingResponse(PingRequest request) {
        MessageId messageId = request.getMessageId();
        Contact2 destination = request.getContact();
        return new DefaultPingResponse(messageId, contact, destination);
    }

    @Override
    public NodeRequest createNodeRequest(Contact2 dst, KUID key) {
        MessageId messageId = createMessageId(dst.getRemoteAddress());
        return new DefaultNodeRequest(messageId, contact, dst, key);
    }

    @Override
    public NodeResponse createNodeResponse(LookupRequest request,
            Contact2[] contacts) {
        MessageId messageId = request.getMessageId();
        Contact2 destination = request.getContact();
        return new DefaultNodeResponse(messageId, contact, 
                destination, contacts);
    }

    @Override
    public ValueRequest createValueRequest(Contact2 dst, KUID key) {
        MessageId messageId = createMessageId(dst.getRemoteAddress());
        return new DefaultValueRequest(messageId, contact, dst, key);
    }

    @Override
    public ValueResponse createValueResponse(LookupRequest request, byte[] value) {
        MessageId messageId = request.getMessageId();
        Contact2 destination = request.getContact();
        return new DefaultValueResponse(messageId, contact, destination, value);
    }

    @Override
    public StoreRequest createStoreRequest(Contact2 dst, KUID key, byte[] value) {
        MessageId messageId = createMessageId(dst.getRemoteAddress());
        return new DefaultStoreRequest(messageId, contact, dst, key, value);
    }

    @Override
    public StoreResponse createStoreResponse(StoreRequest request, Condition status) {
        MessageId messageId = request.getMessageId();
        Contact2 destination = request.getContact();
        return new DefaultStoreResponse(messageId, contact, destination, status);
    }
}
