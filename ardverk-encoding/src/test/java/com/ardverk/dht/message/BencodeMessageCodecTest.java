package com.ardverk.dht.message;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

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
        Contact contact = new DefaultContact(Type.SOLICITED, 
                contactId, 0, new InetSocketAddress("localhost", 6666));
        PingRequest request = new DefaultPingRequest(messageId, contact, 
                System.currentTimeMillis(), InetAddress.getByName("localhost"));
        
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
