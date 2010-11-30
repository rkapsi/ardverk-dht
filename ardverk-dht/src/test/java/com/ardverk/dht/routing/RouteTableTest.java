package com.ardverk.dht.routing;

import java.net.InetSocketAddress;

import junit.framework.TestCase;

import org.junit.Test;

import com.ardverk.dht.KUID;

public class RouteTableTest {

    private static final int ID_SIZE = 20;
    
    private static final int DEFAULT_PORT = 2000;
    
    @Test
    public void initialState() {
        Contact localhost = createLocalhost();
        DefaultRouteTable routeTable 
            = new DefaultRouteTable(localhost);
        
        TestCase.assertEquals(1, routeTable.size());
        TestCase.assertEquals(1, routeTable.getBuckets().length);
        
        TestCase.assertEquals(localhost, routeTable.getLocalhost());
        TestCase.assertSame(localhost, routeTable.getLocalhost());
        
        TestCase.assertEquals(localhost, routeTable.get(localhost.getId()));
        TestCase.assertSame(localhost, routeTable.get(localhost.getId()));
        
        ContactEntity[] active = routeTable.getActiveContacts();
        ContactEntity[] cached = routeTable.getCachedContacts();
        
        TestCase.assertEquals(1, active.length);
        TestCase.assertEquals(0, cached.length);
        
        TestCase.assertEquals(localhost, active[0].getContact());
        TestCase.assertSame(localhost, active[0].getContact());
    }
    
    @Test
    public void localhost() {
        Contact localhost = createLocalhost();
        DefaultRouteTable routeTable 
            = new DefaultRouteTable(localhost);
        
        // Adding it *AGAIN*
        routeTable.add(localhost);
        
        // Nothing should change.
        initialState();
    }
    
    private static Contact createLocalhost() {
        return createLocalhost(DEFAULT_PORT);
    }
    
    private static Contact createLocalhost(int port) {
        return createLocalhost(KUID.createRandom(ID_SIZE), port);
    }
    
    private static Contact createLocalhost(KUID contactId, int port) {
        return Contact.localhost(contactId, 
                new InetSocketAddress("localhost", port));
    }
}
