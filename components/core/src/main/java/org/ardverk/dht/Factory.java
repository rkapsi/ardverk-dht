package org.ardverk.dht;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import org.ardverk.dht.routing.Identity;
import org.ardverk.dht.storage.Datastore;
import org.ardverk.dht.storage.TransientDatastore;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;

public class Factory {
    
    private static final int SHA1 = 20;
    
    public static Factory sha1() {
        return new Factory(SHA1);
    }
    
    private final int keySize;
    
    private Factory(int keySize) {
        this.keySize = keySize;
    }
    
    public DHT newDHT(int port) {
        return newDHT(new InetSocketAddress(port));
    }
    
    public DHT newDHT(int port, Datastore datastore) {
        return newDHT(new InetSocketAddress(port), datastore);
    }
    
    public DHT newDHT(SocketAddress address) {
        return newDHT(address, new TransientDatastore(30L, TimeUnit.MINUTES));
    }
    
    public DHT newDHT(SocketAddress address, Datastore datastore) {
        return createInjector(address, datastore).getInstance(DHT.class);
    }
    
    public Injector createInjector(final SocketAddress address, final Datastore datastore) {
        Module module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(Datastore.class).toInstance(datastore);
            }
            
            @Provides @Singleton
            Identity getIdentity() {
                return new Identity(keySize, address);
            }
        };
        
        return Guice.createInjector(module);
    }
}
