package com.ardverk.dht.message;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.junit.Test;

import com.ardverk.dht.KUID;
import com.ardverk.dht.io.session.SessionContext;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.DefaultContact;
import com.ardverk.dht.routing.Contact.Type;

public class BencodeMessageCodecTest {

    @Test
    public void encodeDecode() throws IOException {
        BencodeMessageCodec codec 
            = new BencodeMessageCodec();
        
        SessionContext context = new DefaultSessionContext();
        MessageId messageId = new MessageId(new byte[20]);
        KUID contactId = new KUID(new byte[20]);
        Contact source = new DefaultContact(Type.SOLICITED, 
                contactId, 0, new InetSocketAddress("localhost", 6666));
        
        Contact destination = new DefaultContact(Type.SOLICITED, 
                contactId, 0, new InetSocketAddress("localhost", 6666));
        
        PingRequest request = new DefaultPingRequest(messageId, source, 
                destination, System.currentTimeMillis());
        
        byte[] data = codec.encode(context, request);
        Message message = codec.decode(context, data);
        
        System.out.println(message);
    }
    
    private static class DefaultSessionContext implements SessionContext {

        @Override
        public InetSocketAddress getLocalAddress() {
            return new InetSocketAddress("localhost", 6666);
        }

        @Override
        public InetSocketAddress getRemoteAddress() {
            return new InetSocketAddress("localhost", 6666);
        }
    }
}
