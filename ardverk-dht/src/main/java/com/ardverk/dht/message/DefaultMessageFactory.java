package com.ardverk.dht.message;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.DefaultContact;
import com.ardverk.dht.routing.Contact.Type;

public class DefaultMessageFactory extends AbstractMessageFactory {

    private final Contact contact;
    
    public DefaultMessageFactory(int length) {
        super(length);
        
        KUID contactId = new KUID(new byte[] { 4, 5, 6 });
        
        contact = new DefaultContact(
                Type.SOLICITED, 
                contactId, 0, 
                new InetSocketAddress("localhost", 6666),
                new InetSocketAddress("localhost", 6666));
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
    public NodeResponse createNodeResponse(NodeRequest request,
            Contact[] contacts) {
        MessageId messageId = request.getMessageId();
        Contact destination = request.getContact();
        return new DefaultNodeResponse(messageId, contact, 
                destination, contacts);
    }
}
