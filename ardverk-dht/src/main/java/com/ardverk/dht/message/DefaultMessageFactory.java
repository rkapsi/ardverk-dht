package com.ardverk.dht.message;

import java.net.SocketAddress;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact;

public class DefaultMessageFactory extends AbstractMessageFactory {

    private final Contact contact;
    
    public DefaultMessageFactory(int length, Contact contact) {
        super(length);
        
        if (contact == null) {
            throw new NullPointerException("contact");
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
        MessageId messageId = request.getMessageId();
        Contact destination = request.getContact();
        return new DefaultPingResponse(messageId, contact, destination);
    }

    @Override
    public NodeRequest createNodeRequest(Contact dst, KUID key) {
        MessageId messageId = createMessageId(dst.getRemoteAddress());
        return new DefaultNodeRequest(messageId, contact, dst, key);
    }

    @Override
    public NodeResponse createNodeResponse(LookupRequest request,
            Contact[] contacts) {
        MessageId messageId = request.getMessageId();
        Contact destination = request.getContact();
        return new DefaultNodeResponse(messageId, contact, 
                destination, contacts);
    }
}
