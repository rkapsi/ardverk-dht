package com.ardverk.dht.message;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.junit.Test;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.Contact.Type;

public class BencodeMessageCodecTest {

    @Test
    public void encodeDecode() throws IOException {
        BencodeMessageCodec codec 
            = new BencodeMessageCodec();
        
        MessageId messageId = MessageId.createRandom(20);
        KUID contactId = KUID.createRandom(20);
        
        Contact contact = new Contact(Type.SOLICITED, 
                contactId, 0, 
                new InetSocketAddress("localhost", 6666));
        
        SocketAddress address = new InetSocketAddress("localhost", 6666);
        PingRequest request = new DefaultPingRequest(messageId, contact, address);
        
        byte[] data = codec.encode(request);
        Message message = codec.decode(address, data);
        
        System.out.println(message);
    }
}
