package com.ardverk.dht.io;

import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.ardverk.io.IoUtils;
import org.junit.Test;

import com.ardverk.dht.ArdverkDHT;
import com.ardverk.dht.ArdverkUtils;
import com.ardverk.dht.KUID;
import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.config.DefaultLookupConfig;
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.utils.XorComparator;

public class NodeResponseHandlerTest {

    @Test
    public void lookup() throws Exception {
        List<ArdverkDHT> dhts = ArdverkUtils.createDHTs(256, 2000);
        try {
            ArdverkUtils.bootstrap(dhts);
            
            KUID lookupId = KUID.createRandom(20);
            
            TreeSet<KUID> expected = new TreeSet<KUID>(new XorComparator(lookupId));
            for (ArdverkDHT dht : dhts) {
                expected.add(dht.getLocalhost().getId());
            }
            
            ArdverkDHT first = dhts.get(0);
            ArdverkFuture<NodeEntity> future 
                = first.lookup(lookupId, new DefaultLookupConfig());
            NodeEntity entity = future.get();
            TestCase.assertEquals(lookupId, entity.getId());
            
            Contact[] contacts = entity.getContacts();
            KUID[] contactIds = new KUID[contacts.length];
            for (int i = 0; i < contacts.length; i++) {
                contactIds[i] = contacts[i].getId();
            }
            
            int k = first.getRouteTable().getK();
            
            for (int i = 0; i < k && i < contacts.length; i++) {
                KUID contactId = contacts[i].getId();
                KUID expectedId = expected.pollFirst();
                
                //TestCase.assertEquals(expectedId, contactId);
                
                if (!contactId.equals(expectedId)) {
                    ArdverkDHT dht = get(dhts, expectedId);
                    NodeRequestHandler h = ((DefaultMessageDispatcher)
                            dht.getMessageDispatcher()).getNodeRequestHandler();
                    List<KUID> c = h.c;
                    
                    System.out.println("INDEX: " + c.indexOf(
                            first.getLocalhost().getId()) + ", " + c.size() 
                            + ", " + Arrays.asList(contactIds).contains(expectedId));
                }
            }
            
        } finally {
            IoUtils.closeAll(dhts);
        }
    }
    
    private static void isCloserTo(KUID lookupId, Contact[] contacts) {
        Arrays.sort(contacts, new XorComparator(lookupId));
        
        for (int i = 1; i < contacts.length; i++) {
            TestCase.assertTrue(contacts[i-1].getId().isCloserTo(
                    lookupId, contacts[i].getId()));
        }
        
        TestCase.assertTrue(contacts[0].getId().isCloserTo(
                    lookupId, contacts[contacts.length-1].getId()));
    }
    
    private static ArdverkDHT get(List<ArdverkDHT> dhts, KUID contactId) {
        for (ArdverkDHT dht : dhts) {
            if (dht.getLocalhost().getId().equals(contactId)) {
                return dht;
            }
        }
        return null;
    }
}
