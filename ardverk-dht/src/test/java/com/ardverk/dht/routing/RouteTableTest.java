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

package com.ardverk.dht.routing;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.junit.Test;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.IContact.Type;

public class RouteTableTest {

    private static final int K = 20;
    
    private static final int ID_SIZE = 20;
    
    private static final int DEFAULT_PORT = 2000;
    
    @Test
    public void initialState() {
        DefaultContact localhost = createLocalhost();
        DefaultRouteTable routeTable = createRouteTable(localhost);
        
        TestCase.assertEquals(1, routeTable.size());
        TestCase.assertEquals(1, routeTable.getBuckets().length);
        
        TestCase.assertEquals(localhost, routeTable.getLocalhost());
        TestCase.assertSame(localhost, routeTable.getLocalhost());
        
        TestCase.assertEquals(localhost, routeTable.get(localhost.getId()));
        TestCase.assertSame(localhost, routeTable.get(localhost.getId()));
        
        ContactEntry[] active = routeTable.getActiveContacts();
        ContactEntry[] cached = routeTable.getCachedContacts();
        
        TestCase.assertEquals(1, active.length);
        TestCase.assertEquals(0, cached.length);
        
        TestCase.assertEquals(localhost, active[0].getContact());
        TestCase.assertSame(localhost, active[0].getContact());
    }
    
    @Test
    public void addLocalhost() {
        DefaultContact localhost = createLocalhost();
        DefaultRouteTable routeTable = createRouteTable(localhost);
        
        routeTable.addRouteTableListener(new RouteTableAdapter() {
            @Override
            public void handleContactAdded(Bucket bucket, IContact contact) {
                TestCase.fail("Shouldn't have been called.");
            }
        });
        
        // Adding it *AGAIN*
        routeTable.add(localhost);
        
        // Nothing should change.
        initialState();
    }
    
    @Test
    public void addContact() throws InterruptedException {
        DefaultRouteTable routeTable = createRouteTable();
        
        final CountDownLatch latch = new CountDownLatch(1);
        routeTable.addRouteTableListener(new RouteTableAdapter() {
            @Override
            public void handleContactAdded(Bucket bucket, IContact contact) {
                latch.countDown();
            }
        });
        
        DefaultContact contact = createContact();
        routeTable.add(contact);
        
        if (!latch.await(1L, TimeUnit.SECONDS)) {
            TestCase.fail("Shouldn't have failed!");
        }
        
        TestCase.assertEquals(2, routeTable.size());
        TestCase.assertEquals(1, routeTable.getBuckets().length);
        TestCase.assertEquals(2, routeTable.getBuckets()[0].getActiveCount());
    }
    
    @Test
    public void split() throws InterruptedException {
        final DefaultRouteTable routeTable = createRouteTable();
        
        while (routeTable.size() < routeTable.getK()) {
            DefaultContact contact = createContact();
            routeTable.add(contact);
        }
        
        TestCase.assertEquals(routeTable.getK(), routeTable.size());
        TestCase.assertEquals(1, routeTable.getBuckets().length);
        
        final CountDownLatch latch = new CountDownLatch(1);
        routeTable.addRouteTableListener(new RouteTableAdapter() {
            @Override
            public void handleBucketSplit(Bucket bucket, Bucket left,
                    Bucket right) {
                latch.countDown();
            }
        });
        
        DefaultContact contact = createContact();
        routeTable.add(contact);
        
        if (!latch.await(1L, TimeUnit.SECONDS)) {
            TestCase.fail("Shouldn't have failed!");
        }
        
        TestCase.assertEquals(routeTable.getK() + 1, routeTable.size());
        TestCase.assertEquals(2, routeTable.getBuckets().length);
    }
    
    @Test
    public void select() {
        DefaultContact localhost = createLocalhost();
        DefaultRouteTable routeTable = createRouteTable(localhost);
        
        while (routeTable.size() < routeTable.getK()) {
            DefaultContact contact = createContact();
            routeTable.add(contact);
        }
        
        IContact[] contacts = routeTable.select(localhost.getId());
        TestCase.assertEquals(routeTable.getK(), contacts.length);
        
        TestCase.assertEquals(localhost, contacts[0]);
    }
    
    private static DefaultRouteTable createRouteTable() {
        DefaultContact localhost = createLocalhost();
        return createRouteTable(localhost);
    }
    
    private static DefaultRouteTable createRouteTable(DefaultContact localhost) {
        return new DefaultRouteTable(K, localhost);
    }
    
    private static DefaultContact createLocalhost() {
        return createLocalhost(DEFAULT_PORT);
    }
    
    private static DefaultContact createLocalhost(int port) {
        return createLocalhost(KUID.createRandom(ID_SIZE), port);
    }
    
    private static DefaultContact createLocalhost(KUID contactId, int port) {
        return DefaultContact.localhost(contactId, 
                new InetSocketAddress("localhost", port));
    }
    
    private static DefaultContact createContact() {
        return createContact(DEFAULT_PORT);
    }
    
    private static DefaultContact createContact(int port) {
        return createContact(KUID.createRandom(ID_SIZE), port);
    }
    
    private static DefaultContact createContact(KUID contactId, int port) {
        return new DefaultContact(Type.SOLICITED, contactId, 
                0, new InetSocketAddress("localhost", port));
    }
}