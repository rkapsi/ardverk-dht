package com.ardverk.dht.message;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.junit.Test;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact2;
import com.ardverk.dht.routing.Contact2.Type;

public class BencodeMessageCodecTest {

    @Test
    public void encodeDecode() throws IOException {
        BencodeMessageCodec codec 
            = new BencodeMessageCodec();
        
        MessageId messageId = new MessageId(new byte[20]);
        KUID contactId = new KUID(new byte[20]);
        Contact2 contact = new Contact2(Type.SOLICITED, 
                contactId, 0, 
                new InetSocketAddress("localhost", 6666));
        
        SocketAddress address = new InetSocketAddress("localhost", 6666);
        PingRequest request = new DefaultPingRequest(messageId, contact, address);
        
        byte[] data = codec.encode(request);
        Message message = codec.decode(null, data);
        
        System.out.println(message);
    }
}
