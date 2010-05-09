package com.ardverk.dht.message;

import java.net.SocketAddress;

import org.ardverk.lang.NullArgumentException;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.storage.Database.Condition;

public class DefaultMessageFactory extends AbstractMessageFactory {

    private final Contact contact;
    
    public DefaultMessageFactory(int length, Contact contact) {
        super(length);
        
        if (contact == null) {
            throw new NullArgumentException("contact");
        }
        
        this.contact = contact;
    }
    
    @Override
    public PingRequest createPingRequest(Contact dst) {
        return createPingRequest(dst.getRemoteAddress());
    }

    @Override
    public PingRequest createPingRequest(SocketAddress dst) {
        MessageId messageId = createMessageId(dst);
        return new DefaultPingRequest(messageId, contact, dst);
    }

    @Override
    public PingResponse createPingResponse(PingRequest request) {
        Contact dst = request.getContact();
        SocketAddress address = dst.getRemoteAddress();
        MessageId messageId = request.getMessageId();
        return new DefaultPingResponse(messageId, contact, address);
    }

    @Override
    public NodeRequest createNodeRequest(Contact dst, KUID key) {
        SocketAddress address = dst.getRemoteAddress();
        MessageId messageId = createMessageId(address);
        return new DefaultNodeRequest(messageId, contact, address, key);
    }

    @Override
    public NodeResponse createNodeResponse(LookupRequest request, Contact[] contacts) {
        Contact dst = request.getContact();
        SocketAddress address = dst.getRemoteAddress();
        MessageId messageId = request.getMessageId();
        return new DefaultNodeResponse(messageId, contact, address, contacts);
    }

    @Override
    public ValueRequest createValueRequest(Contact dst, KUID key) {
        SocketAddress address = dst.getRemoteAddress();
        MessageId messageId = createMessageId(address);
        return new DefaultValueRequest(messageId, contact, address, key);
    }

    @Override
    public ValueResponse createValueResponse(LookupRequest request, byte[] value) {
        Contact dst = request.getContact();
        SocketAddress address = dst.getRemoteAddress();
        MessageId messageId = request.getMessageId();
        return new DefaultValueResponse(messageId, contact, address, value);
    }

    @Override
    public StoreRequest createStoreRequest(Contact dst, KUID key, byte[] value) {
        SocketAddress address = dst.getRemoteAddress();
        MessageId messageId = createMessageId(address);
        return new DefaultStoreRequest(messageId, contact, address, key, value);
    }

    @Override
    public StoreResponse createStoreResponse(StoreRequest request, Condition status) {
        Contact dst = request.getContact();
        SocketAddress address = dst.getRemoteAddress();
        MessageId messageId = request.getMessageId();
        return new DefaultStoreResponse(messageId, contact, address, status);
    }
}
