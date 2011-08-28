package org.ardverk.dht;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import org.ardverk.dht.message.DefaultMessageFactory;
import org.ardverk.dht.message.MessageFactory;
import org.ardverk.dht.routing.DefaultRouteTable;
import org.ardverk.dht.routing.Localhost;
import org.ardverk.dht.routing.RouteTable;
import org.ardverk.dht.storage.Datastore;
import org.ardverk.dht.storage.SimpleDatastore;

public class Builder {

    public static final int SHA1 = 20;
    
    public static final int MD5 = 16;
    
    public static Builder sha1() {
        return valueOf(SHA1);
    }
    
    public static Builder md5() {
        return valueOf(MD5);
    }
    
    public static Builder valueOf(int keySize) {
        return new Builder(keySize);
    }
    
    private final int keySize;
    
    private long frequencyInMillis = TimeUnit.MINUTES.toMillis(30L);
    
    private long timeoutInMillis = frequencyInMillis;
    
    private Builder(int keySize) {
        this.keySize = keySize;
    }
    
    public Builder setDatastoreFrequency(long frequency, TimeUnit unit) {
        this.frequencyInMillis = unit.toMillis(frequency);
        return this;
    }
    
    public Builder setDatastoreTimeout(long timeout, TimeUnit unit) {
        this.timeoutInMillis = unit.toMillis(timeout);
        return this;
    }
    
    public DHT newDHT() {
        Localhost localhost = new Localhost(keySize);
        
        MessageFactory messageFactory 
            = new DefaultMessageFactory(localhost);
        
        Datastore datastore 
            = new SimpleDatastore(frequencyInMillis, 
                timeoutInMillis, TimeUnit.MILLISECONDS);
        
        RouteTable routeTable = new DefaultRouteTable(localhost);
        
        return new ArdverkDHT(messageFactory, routeTable, datastore);
    }
    
    public DHT newDHT(int port) throws IOException {
        DHT dht = newDHT();
        dht.bind(port);
        return dht;
    }
    
    public DHT newDHT(String host, int port) throws IOException {
        DHT dht = newDHT();
        dht.bind(host, port);
        return dht;
    }
    
    public DHT newDHT(InetAddress addr, int port) throws IOException {
        DHT dht = newDHT();
        dht.bind(addr, port);
        return dht;
    }
    
    public DHT newDHT(SocketAddress bindaddr) throws IOException {
        DHT dht = newDHT();
        dht.bind(bindaddr);
        return dht;
    }
}
