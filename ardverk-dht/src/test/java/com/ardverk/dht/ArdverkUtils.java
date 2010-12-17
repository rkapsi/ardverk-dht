/*
 * Copyright 2009-2010 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ardverk.dht;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.ardverk.io.IoUtils;
import org.ardverk.security.MessageDigestUtils;
import org.ardverk.utils.StringUtils;

import com.ardverk.dht.SimpleArdverkDHT.SimpleConfig;
import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.config.BootstrapConfig;
import com.ardverk.dht.config.DefaultBootstrapConfig;
import com.ardverk.dht.config.DefaultPutConfig;
import com.ardverk.dht.config.DefaultQuickenConfig;
import com.ardverk.dht.entity.BootstrapEntity;
import com.ardverk.dht.entity.PutEntity;
import com.ardverk.dht.entity.QuickenEntity;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.DefaultRouteTable;
import com.ardverk.dht.storage.Database;

public class ArdverkUtils {

    private static final String SECRET_KEY = "90fb237cbec71523ba9d883a8ec6ae9f";
    private static final String INIT_VECTOR = "6fd7bda068bf2425980e5c9b1c9e2097";
    
    private ArdverkUtils() {}
    
    public static ArdverkDHT createDHT(int port) throws IOException {
        return createDHT(new InetSocketAddress("localhost", port));
    }
    
    public static ArdverkDHT createDHT(SocketAddress address) throws IOException {
        SimpleConfig config = new SimpleConfig();
        config.setSecretKey(SECRET_KEY);
        config.setInitVector(INIT_VECTOR);
        return SimpleArdverkDHT.create(config, address);
    }
    
    public static List<ArdverkDHT> createDHTs(int count, int port) throws IOException {
        List<ArdverkDHT> dhts = new ArrayList<ArdverkDHT>(count);
        
        try {
            for (int i = 0; i < count; i++) {
                dhts.add(createDHT(port + i));
            }
        } catch (IOException err) {
            IoUtils.closeAll(dhts);
            throw err;
        }
        
        return dhts;
    }
    
    public static List<ArdverkFuture<BootstrapEntity>> bootstrap(List<? extends DHT> dhts) 
            throws InterruptedException, ExecutionException {
        
        if (dhts.size() <= 1) {
            throw new IllegalArgumentException();
        }
        
        // Bootstrap everyone from the first DHT
        DHT first = dhts.get(0);
        List<ArdverkFuture<BootstrapEntity>> futures1 
            = bootstrap(first.getLocalhost(), dhts, 1, dhts.size()-1);
        
        // The RouteTable is all messed up! Clear it and bootstrap
        // the first DHT from the others.
        ((DefaultRouteTable)first.getRouteTable()).clear();
        List<ArdverkFuture<BootstrapEntity>> futures2 
            = bootstrap(dhts.get(1).getLocalhost(), dhts, 0, 1);
        
        futures2.addAll(futures1);
        return futures2;
    }
    
    public static List<ArdverkFuture<BootstrapEntity>> bootstrap(Contact from, 
            List<? extends DHT> dhts, int offset, int length) 
                throws InterruptedException, ExecutionException {
        
        List<ArdverkFuture<BootstrapEntity>> futures 
            = new ArrayList<ArdverkFuture<BootstrapEntity>>();
        
        BootstrapConfig config = new DefaultBootstrapConfig();
        config.setQueueKey(QueueKey.BACKEND);
        
        for (int i = 0; i < length; i++) {
            ArdverkFuture<BootstrapEntity> future 
                = dhts.get(offset+i).bootstrap(from, config);
            futures.add(future);
            future.get();
        }
        
        return futures;
    }
    
    public static List<ArdverkFuture<QuickenEntity>> refresh(List<? extends DHT> dhts) 
            throws InterruptedException, ExecutionException {
        return refresh(dhts, 0, dhts.size());
    }
    
    public static List<ArdverkFuture<QuickenEntity>> refresh(List<? extends DHT> dhts, int offset, int length) 
            throws InterruptedException, ExecutionException {
        
        List<ArdverkFuture<QuickenEntity>> futures 
            = new ArrayList<ArdverkFuture<QuickenEntity>>();
        
        DefaultQuickenConfig config = new DefaultQuickenConfig();
        config.setQueueKey(QueueKey.BACKEND);
        
        for (int i = 0; i < length; i++) {
            config.setBucketTimeout(-1L, TimeUnit.MILLISECONDS);
            //config.setBucketTimeout(1L, TimeUnit.MINUTES);
            
            ArdverkFuture<QuickenEntity> future 
                = ((ArdverkDHT)dhts.get(offset + i)).quicken(config);
            futures.add(future);
            future.get();
        }
        return futures;
    }
    
    private static ArdverkDHT get(List<ArdverkDHT> dhts, Contact contact) {
        for (ArdverkDHT dht : dhts) {
            if (dht.getLocalhost().equals(contact)) {
                return dht;
            }
        }
        
        throw new IllegalStateException();
    }
    
    public static void main(String[] args) 
            throws IOException, InterruptedException, ExecutionException {
        
        System.setProperty("networkaddress.cache.ttl", "0");
        
        final List<ArdverkDHT> dhts = createDHTs(256, 2000);
        bootstrap(dhts);
        
        /*DHT x = dhts.get(0);
        PainterFrame frame = new PainterFrame(x);
        
        Contact localhost = x.getLocalhost();
        frame.setTitle(localhost.getId() + ":" + NetworkUtils.getPort(
                localhost.getSocketAddress()));
        frame.setVisible(true);
        frame.start();*/
        
        for (ArdverkDHT dht : dhts) {
            Database database = dht.getDatabase();
            database.getDatabaseConfig().setStoreForward(false);
            database.getDatabaseConfig().setCheckBucket(false);
        }
        
        final DefaultPutConfig putConfig = new DefaultPutConfig();
        putConfig.setQueueKey(QueueKey.BACKEND);
        
        int count = 30000;
        ArdverkFuture<PutEntity> future = null;
        for (int i = 0; i < count; i++) {
            MessageDigest md = MessageDigestUtils.createSHA1();
            md.update(StringUtils.getBytes("Hello-" + i));
            
            final KUID key = KUID.create(md.digest());
            final byte[] value = StringUtils.getBytes("World-" + i);
            
            int rnd = (int)(dhts.size() * Math.random());
            future = dhts.get(rnd).put(key, value, putConfig);
            
            if (i % 1000 == 0) {
                System.out.println("PROGRESS: " + i);
            }
        }
        
        try {
            if (future != null) {
                System.out.println("GETTING..." + new Date());
                System.out.println(future.get());
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
        
        System.out.println("DONE..." + new Date());
        
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        
        for (ArdverkDHT dht : dhts) {
            Database database = dht.getDatabase();
            int size = database.size();
            
            if (size < min) {
                min = size;
            }
            
            if (max < size) {
                max = size;
            }
        }
        
        System.out.println("MIN: " + min);
        System.out.println("MAX: " + max);
    }
}