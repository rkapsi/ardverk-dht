package com.ardverk.dht;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.ardverk.utils.DeadlockScanner;

import com.ardverk.dht.io.BootstrapProcess.Config;

public class Main {
    
    public static void main(String[] args) throws IOException, 
            InterruptedException, ExecutionException {
        
        DeadlockScanner.start();
        
        List<DHT> list = new ArrayList<DHT>();
        
        for (int i = 0; i < 100; i++) {
            int port = 2000 + i;
            
            DHT dht = new ArdverkDHT(port);
            list.add(dht);
        }
        
        for (int i = 1; i < list.size(); i++) {
            Config config = new Config(new InetSocketAddress("localhost", 2000));
            list.get(i).bootstrap(config, 1L, TimeUnit.MINUTES).get();
        }
        
        Config config = new Config(new InetSocketAddress("localhost", 2001));
        list.get(0).bootstrap(config, 1L, TimeUnit.MINUTES).get();
        
        for (DHT dht : list) {
            config = new Config(new InetSocketAddress("localhost", 2000));
            dht.bootstrap(config, 1L, TimeUnit.MINUTES).get();
        }
        
        for (DHT dht : list) {
            System.out.println(dht.getRouteTable().size());
        }
        
        System.out.println("DONE!");
    }
}
