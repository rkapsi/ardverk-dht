package org.ardverk.dht;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import org.ardverk.dht.message.DefaultMessageFactory;
import org.ardverk.dht.message.MessageFactory;
import org.ardverk.dht.routing.DefaultRouteTable;
import org.ardverk.dht.routing.Localhost;
import org.ardverk.dht.routing.RouteTable;
import org.ardverk.dht.storage.Datastore;
import org.ardverk.dht.storage.PersistentDatastore;
import org.ardverk.dht.storage.TransientDatastore;
import org.ardverk.net.NetworkUtils;

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
    
    private boolean useTransientStore = true;
    
    private File directory = null;
    
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
    
    public Builder setUseTransientStore(boolean useTransientStore) {
        this.useTransientStore = useTransientStore;
        return this;
    }
    
    public Builder setDatastoreLocation(File directory) {
        this.directory = directory;
        return this;
    }
    
    public DHT newDHT(int port) {
        return newDHT(new InetSocketAddress(port));
    }
    
    public DHT newDHT(String host, int port) {
        return newDHT(NetworkUtils.createUnresolved(host, port));
    }
    
    public DHT newDHT(InetAddress address, int port) {
        return newDHT(new InetSocketAddress(address, port));
    }
    
    public DHT newDHT(SocketAddress address) {
        
        Localhost localhost = new Localhost(keySize, address);
        
        MessageFactory messageFactory 
            = new DefaultMessageFactory(localhost);
        
        Datastore datastore = null;
        if (useTransientStore) {
            datastore = new TransientDatastore(frequencyInMillis, 
                    timeoutInMillis, TimeUnit.MILLISECONDS);
        } else {
            
            if (directory == null) {
                directory = new File("datastore");
            }
            
            datastore = new PersistentDatastore(directory,
                    frequencyInMillis, timeoutInMillis, TimeUnit.MILLISECONDS);
        }
        
        RouteTable routeTable = new DefaultRouteTable(localhost);
        
        return new ArdverkDHT(messageFactory, routeTable, datastore);
    }
}
