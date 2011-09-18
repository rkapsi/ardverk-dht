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

package org.ardverk.dht.routing;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.ardverk.dht.KUID;
import org.ardverk.dht.routing.Contact.Type;
import org.junit.Test;


public class RouteTableTest {

    private static final int K = 20;
    
    private static final int ID_SIZE = 20;
    
    private static final int DEFAULT_PORT = 2000;
    
    @Test
    public void initialState() {
        Identity localhost = createLocalhost();
        DefaultRouteTable routeTable = createRouteTable(localhost);
        
        TestCase.assertEquals(1, routeTable.size());
        TestCase.assertEquals(1, routeTable.getBuckets().length);
        
        TestCase.assertEquals(localhost, routeTable.getIdentity());
        TestCase.assertSame(localhost, routeTable.getIdentity());
        
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
        Identity localhost = createLocalhost();
        DefaultRouteTable routeTable = createRouteTable(localhost);
        
        routeTable.addRouteTableListener(new RouteTableAdapter() {
            @Override
            public void handleContactAdded(Bucket bucket, Contact contact) {
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
            public void handleContactAdded(Bucket bucket, Contact contact) {
                latch.countDown();
            }
        });
        
        Contact contact = createContact();
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
            Contact contact = createContact();
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
        
        Contact contact = createContact();
        routeTable.add(contact);
        
        if (!latch.await(1L, TimeUnit.SECONDS)) {
            TestCase.fail("Shouldn't have failed!");
        }
        
        TestCase.assertEquals(routeTable.getK() + 1, routeTable.size());
        TestCase.assertEquals(2, routeTable.getBuckets().length);
    }
    
    @Test
    public void select() {
        Identity localhost = createLocalhost();
        DefaultRouteTable routeTable = createRouteTable(localhost);
        
        while (routeTable.size() < routeTable.getK()) {
            Contact contact = createContact();
            routeTable.add(contact);
        }
        
        Contact[] contacts = routeTable.select(localhost.getId());
        TestCase.assertEquals(routeTable.getK(), contacts.length);
        
        TestCase.assertEquals(localhost, contacts[0]);
    }
    
    private static DefaultRouteTable createRouteTable() {
        Identity localhost = createLocalhost();
        return createRouteTable(localhost);
    }
    
    private static DefaultRouteTable createRouteTable(Identity localhost) {
        return new DefaultRouteTable(new RouteTableConfig(K), localhost);
    }
    
    private static Identity createLocalhost() {
        return createLocalhost(ID_SIZE);
    }
    
    private static Identity createLocalhost(int keySize) {
        return new Identity(keySize, new InetSocketAddress(DEFAULT_PORT));
    }
    
    private static Contact createContact() {
        return createContact(DEFAULT_PORT);
    }
    
    private static Contact createContact(int port) {
        return createContact(KUID.createRandom(ID_SIZE), port);
    }
    
    private static Contact createContact(KUID contactId, int port) {
        return new DefaultContact(Type.SOLICITED, contactId, 
                0, false, new InetSocketAddress("localhost", port));
    }
}