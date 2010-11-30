package com.ardverk.dht;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.ardverk.utils.DeadlockScanner;

import com.ardverk.dht.codec.DefaultMessageCodec;
import com.ardverk.dht.codec.MessageCodec;
import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.config.BootstrapConfig;
import com.ardverk.dht.config.DefaultBootstrapConfig;
import com.ardverk.dht.config.DefaultPutConfig;
import com.ardverk.dht.config.DefaultRefreshConfig;
import com.ardverk.dht.config.DefaultValueConfig;
import com.ardverk.dht.entity.BootstrapEntity;
import com.ardverk.dht.entity.RefreshEntity;
import com.ardverk.dht.entity.StoreEntity;
import com.ardverk.dht.entity.ValueEntity;
import com.ardverk.dht.io.transport.DatagramTransport;
import com.ardverk.dht.io.transport.Transport;
import com.ardverk.dht.message.DefaultMessageFactory;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.DefaultRouteTable;
import com.ardverk.dht.routing.RouteTable;
import com.ardverk.dht.storage.Database;
import com.ardverk.dht.storage.DefaultDatabase;
import com.ardverk.dht.storage.DefaultValue;
import com.ardverk.dht.storage.Value;
import com.ardverk.dht.storage.ValueTuple;

public class Main {
    
    private static final String SECRET_KEY = "90fb237cbec71523ba9d883a8ec6ae9f";
    private static final String INIT_VECTOR = "6fd7bda068bf2425980e5c9b1c9e2097";
    
    private static final int ID_SIZE = 20;
    
    private static DHT createDHT(int port) throws IOException {
        Contact localhost = Contact.localhost(KUID.createRandom(ID_SIZE), 
                new InetSocketAddress("localhost", port));
        
        MessageCodec codec = new DefaultMessageCodec(SECRET_KEY, INIT_VECTOR);
        
        MessageFactory messageFactory = new DefaultMessageFactory(
                ID_SIZE, localhost);
        
        Database database = new DefaultDatabase();
        RouteTable routeTable = new DefaultRouteTable(localhost);
        
        DHT dht = new ArdverkDHT(codec, messageFactory, routeTable, database);
        
        Transport transport = new DatagramTransport(port);
        dht.getMessageDispatcher().bind(transport);
        
        return dht;
    }
    
    public static void main(String[] args) throws Exception, 
            InterruptedException, ExecutionException {
        
        DeadlockScanner.start();
        
        DHT first = null;
        List<DHT> dhts = new ArrayList<DHT>();
        for (int i = 0; i < 128; i++) {
            DHT dht = createDHT(2000 + i);
            dhts.add(dht);
            
            if (first == null) {
                first = dht;
            }
            
            if (0 < i) {
                BootstrapConfig config = new DefaultBootstrapConfig();
                config.setQueueKey(QueueKey.BACKEND);
                
                ArdverkFuture<BootstrapEntity> future = dht.bootstrap(
                        first.getLocalhost(), config);
                future.get();
            }
        }
        
        int index = 0;
        for (DHT dht : dhts) {
            DefaultRefreshConfig config = new DefaultRefreshConfig();
            config.setQueueKey(QueueKey.BACKEND);
            
            config.setBucketTimeout(-1L, TimeUnit.MILLISECONDS);
            
            ArdverkFuture<RefreshEntity> future = dht.refresh(config);
            RefreshEntity entity = future.get();
            System.out.println((index++) + ": " + entity);
        }
        
        System.out.println(dhts.size());
        System.out.println(first.getRouteTable().size());
        
        KUID key = KUID.createRandom(ID_SIZE);
        Value value = new DefaultValue("Hello World".getBytes());
        ArdverkFuture<StoreEntity> putFuture 
            = first.put(key, value, new DefaultPutConfig());
        putFuture.get();
        
        ArdverkFuture<ValueEntity> valueFuture 
            = first.get(key, new DefaultValueConfig());
        ValueEntity entity = valueFuture.get();
        ValueTuple tuple = entity.getValue();
        
        System.out.println(entity.getTimeInMillis());
        System.out.println(tuple.getSender());
        System.out.println(tuple.getKey().getId() + " vs. " + key);
        System.out.println(new String(tuple.getValue().getValue()));
        
        int foo = 0;
        int counter = 0;
        for (DHT dht : dhts) {
            Database database = dht.getDatabase();
            if (!database.isEmpty()) {
                ++counter;
            }
            
            foo += dht.getRouteTable().size();
        }
        
        System.out.println("COUNTER: " + counter);
        System.out.println("AVG: " + foo/dhts.size());
        System.out.println("DONE!");
        System.exit(0);
    }
}
