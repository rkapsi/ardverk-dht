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

package org.ardverk.dht.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.ardverk.dht.Builder;
import org.ardverk.dht.DHT;
import org.ardverk.dht.KUID;
import org.ardverk.dht.codec.bencode.BencodeMessageCodec;
import org.ardverk.dht.concurrent.DHTFuture;
import org.ardverk.dht.concurrent.ExecutorKey;
import org.ardverk.dht.config.BootstrapConfig;
import org.ardverk.dht.config.DefaultBootstrapConfig;
import org.ardverk.dht.config.DefaultLookupConfig;
import org.ardverk.dht.config.LookupConfig;
import org.ardverk.dht.entity.BootstrapEntity;
import org.ardverk.dht.entity.NodeEntity;
import org.ardverk.dht.io.transport.DatagramTransport;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.routing.DefaultRouteTable;
import org.ardverk.dht.utils.XorComparator;
import org.ardverk.io.IoUtils;
import org.junit.Test;


public class NodeResponseHandlerTest {
    
    private static List<DHT> createDHTs(int count, int port) throws IOException {
        Builder builder = Builder.sha1();
        
        List<DHT> dhts = new ArrayList<DHT>(count);
        
        boolean success = false;
        try {
            for (int i = 0; i < count; i++) {
                int prt = port+i;
                
                DHT dht = builder.newDHT(prt); 
                dht.bind(new DatagramTransport(
                        new BencodeMessageCodec(), prt));
                dhts.add(dht);
            }
            success = true;
        } finally {
            if (!success) {
                IoUtils.closeAll(dhts);
            }
        }
        
        return dhts;
    }
    
    private static List<DHTFuture<BootstrapEntity>> bootstrap(List<? extends DHT> dhts) throws InterruptedException, ExecutionException {
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
        TestCase.assertEquals(1, first.getRouteTable().size());
        
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
    @Test
    public void lookup() throws Exception {
        List<DHT> dhts = createDHTs(256, 2000);
        try {
            bootstrap(dhts);
            
            KUID lookupId = KUID.createRandom(20);
            
            // Sort the DHTs by their XOR distance to the given lookupId.
            TreeSet<KUID> expected = new TreeSet<KUID>(
                    new XorComparator(lookupId));
            for (DHT dht : dhts) {
                expected.add(dht.getLocalhost().getId());
            }
            
            DHT first = dhts.get(0);
            
            LookupConfig config = new DefaultLookupConfig();
            config.setLookupTimeout(20L, TimeUnit.SECONDS);
            
            DHTFuture<NodeEntity> future 
                = first.lookup(lookupId, config);
            NodeEntity entity = future.get();
            TestCase.assertEquals(lookupId, entity.getId());
            
            Contact[] contacts = entity.getContacts();
            Contact[] closest = entity.getClosest();
            
            // The Contacts in the response should be in the same order
            // as our DHT instances!
            int k = first.getRouteTable().getK();
            for (int i = 0; i < k && i < contacts.length; i++) {
                KUID contactId = contacts[i].getId();
                KUID closestId = closest[i].getId();
                
                KUID expectedId = expected.pollFirst();
                
                TestCase.assertEquals(expectedId, contactId);
                TestCase.assertEquals(expectedId, closestId);
                
                TestCase.assertSame(closest[i], contacts[i]);
            }
            
        } finally {
            IoUtils.closeAll(dhts);
        }
    }
}