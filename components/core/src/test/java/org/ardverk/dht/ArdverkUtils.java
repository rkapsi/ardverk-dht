/*
 * Copyright 2009-2011 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ardverk.dht;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.ardverk.dht.codec.MessageCodec;
import org.ardverk.dht.codec.bencode.BencodeMessageCodec;
import org.ardverk.dht.concurrent.DHTFuture;
import org.ardverk.dht.concurrent.ExecutorKey;
import org.ardverk.dht.config.BootstrapConfig;
import org.ardverk.dht.config.DefaultBootstrapConfig;
import org.ardverk.dht.config.DefaultPutConfig;
import org.ardverk.dht.config.DefaultQuickenConfig;
import org.ardverk.dht.easy.EasyConfig;
import org.ardverk.dht.easy.EasyDHT;
import org.ardverk.dht.easy.EasyFactory;
import org.ardverk.dht.entity.BootstrapEntity;
import org.ardverk.dht.entity.PutEntity;
import org.ardverk.dht.entity.QuickenEntity;
import org.ardverk.dht.io.transport.DatagramTransport;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.routing.DefaultRouteTable;
import org.ardverk.dht.rsrc.DefaultKey;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.dht.rsrc.Value;
import org.ardverk.dht.storage.Database;
import org.ardverk.dht.storage.BlobValue;
import org.ardverk.io.IoUtils;
import org.ardverk.utils.StringUtils;
import org.ardverk.version.VectorClock;


public class ArdverkUtils {
    
    private static final MessageCodec CODEC 
        = new BencodeMessageCodec();
    
    private static final EasyConfig CONFIG = new EasyConfig();
    
    private ArdverkUtils() {}
    
    public static EasyDHT createDHT(int port) throws IOException {
        return createDHT(new InetSocketAddress("localhost", port));
    }
    
    public static EasyDHT createDHT(SocketAddress address) throws IOException {
        EasyDHT dht = EasyFactory.create(CONFIG);
        dht.bind(new DatagramTransport(CODEC, address));
        return dht;
    }
    
    public static List<EasyDHT> createDHTs(int count, int port) throws IOException {
        List<EasyDHT> dhts = new ArrayList<EasyDHT>(count);
        
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
    
    public static List<DHTFuture<BootstrapEntity>> bootstrap(List<? extends DHT> dhts) 
            throws InterruptedException, ExecutionException {
        
        if (dhts.size() <= 1) {
            throw new IllegalArgumentException();
        }
        
        // Bootstrap everyone from the first DHT
        DHT first = dhts.get(0);
        List<DHTFuture<BootstrapEntity>> futures1 
            = bootstrap(first.getLocalhost(), dhts, 1, dhts.size()-1);
        
        // The RouteTable is all messed up! Clear it and bootstrap
        // the first DHT from the others.
        ((DefaultRouteTable)first.getRouteTable()).clear();
        List<DHTFuture<BootstrapEntity>> futures2 
            = bootstrap(dhts.get(1).getLocalhost(), dhts, 0, 1);
        
        futures2.addAll(futures1);
        return futures2;
    }
    
    public static List<DHTFuture<BootstrapEntity>> bootstrap(Contact from, 
            List<? extends DHT> dhts, int offset, int length) 
                throws InterruptedException, ExecutionException {
        
        List<DHTFuture<BootstrapEntity>> futures 
            = new ArrayList<DHTFuture<BootstrapEntity>>();
        
        BootstrapConfig config = new DefaultBootstrapConfig();
        config.setExecutorKey(ExecutorKey.BACKEND);
        
        for (int i = 0; i < length; i++) {
            DHTFuture<BootstrapEntity> future 
                = dhts.get(offset+i).bootstrap(from, config);
            futures.add(future);
            future.get();
        }
        
        return futures;
    }
    
    public static List<DHTFuture<QuickenEntity>> refresh(List<? extends DHT> dhts) 
            throws InterruptedException, ExecutionException {
        return refresh(dhts, 0, dhts.size());
    }
    
    public static List<DHTFuture<QuickenEntity>> refresh(List<? extends DHT> dhts, int offset, int length) 
            throws InterruptedException, ExecutionException {
        
        List<DHTFuture<QuickenEntity>> futures 
            = new ArrayList<DHTFuture<QuickenEntity>>();
        
        DefaultQuickenConfig config = new DefaultQuickenConfig();
        config.setExecutorKey(ExecutorKey.BACKEND);
        
        for (int i = 0; i < length; i++) {
            config.setBucketTimeout(-1L, TimeUnit.MILLISECONDS);
            //config.setBucketTimeout(1L, TimeUnit.MINUTES);
            
            DHTFuture<QuickenEntity> future 
                = ((ArdverkDHT)dhts.get(offset + i)).quicken(config);
            futures.add(future);
            future.get();
        }
        return futures;
    }
    
    public static ArdverkDHT get(List<ArdverkDHT> dhts, Contact contact) {
        for (ArdverkDHT dht : dhts) {
            if (dht.getLocalhost().equals(contact)) {
                return dht;
            }
        }
        
        throw new NoSuchElementException("contact=" + contact);
    }
    
    public static void main(String[] args) 
            throws IOException, InterruptedException, ExecutionException {
        
        System.setProperty("networkaddress.cache.ttl", "0");
        
        final List<EasyDHT> dhts = createDHTs(256, 2000);
        bootstrap(dhts);
        
        /*DHT x = dhts.get(0);
        PainterFrame frame = new PainterFrame(x);
        
        Contact localhost = x.getLocalhost();
        frame.setTitle(localhost.getId() + ":" + NetworkUtils.getPort(
                localhost.getSocketAddress()));
        frame.setVisible(true);
        frame.start();*/
        
        for (EasyDHT dht : dhts) {
            Database database = dht.getDatabase();
            database.getDatabaseConfig().setStoreForward(false);
            database.getDatabaseConfig().setCheckBucket(false);
        }
        
        final DefaultPutConfig putConfig = new DefaultPutConfig();
        putConfig.setExecutorKey(ExecutorKey.BACKEND);
        
        int count = 30000;
        DHTFuture<PutEntity> future = null;
        for (int i = 0; i < count; i++) {
            Key key = DefaultKey.valueOf("ardverk://Hello-" + i);
            byte[] data = StringUtils.getBytes("World-" + i);            
            
            VectorClock<KUID> clock = null;

            int rnd = (int)(dhts.size() * Math.random());
            Contact contact = dhts.get(rnd).getLocalhost();
            
            Value value = new BlobValue(contact, clock, data);
            
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
        int total = 0;
        
        for (EasyDHT dht : dhts) {
            Database database = dht.getDatabase();
            int size = database.size();
            total += size;
            
            if (size < min) {
                min = size;
            }
            
            if (max < size) {
                max = size;
            }
        }
        
        System.out.println("MIN: " + min);
        System.out.println("MAX: " + max);
        System.out.println("TOTAL: " + total);
    }
}