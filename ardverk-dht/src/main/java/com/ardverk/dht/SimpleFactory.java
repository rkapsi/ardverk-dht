package com.ardverk.dht;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import com.ardverk.dht.io.mina.MinaTransport;
import com.ardverk.dht.io.transport.Transport;
import com.ardverk.dht.message.AbstractMessageCodec;
import com.ardverk.dht.message.BencodeMessageCodec;
import com.ardverk.dht.message.DefaultMessageFactory;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.DefaultRouteTable;
import com.ardverk.dht.routing.RouteTable;
import com.ardverk.dht.storage.Database;
import com.ardverk.dht.storage.DefaultDatabase;

public class SimpleFactory {
    
    public static final int KEY_SIZE = 20;
    
    public static final int MESSAGE_ID_SIZE = 20;
        
    private SimpleFactory() {}
    
    public static DHT createDHT(int port) {
        return createDHT(new InetSocketAddress("localhost", port));
    }
    
    public static DHT createDHT(SocketAddress address) {
        
        KUID contactId = KUID.createRandom(KEY_SIZE);
        Contact localhost = Contact.localhost(contactId, address);
        
        RouteTable routeTable = new DefaultRouteTable(localhost);
        
        Database database = new DefaultDatabase();
        
        MessageFactory messageFactory = new DefaultMessageFactory(
                MESSAGE_ID_SIZE, localhost);
        
        AbstractMessageCodec codec = new BencodeMessageCodec();
        
        return new ArdverkDHT(codec, messageFactory, routeTable, database);
    }
    
    public static void bind(DHT dht) throws IOException {
        Contact localhost = dht.getLocalhost();
        int port = ((InetSocketAddress)localhost.getRemoteAddress()).getPort();
        
        Transport transport = new MinaTransport(
                new InetSocketAddress(port));
        
        dht.bind(transport);
    }
}
