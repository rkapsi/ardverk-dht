package com.ardverk.dht.io;

import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.ardverk.io.IoUtils;
import org.junit.Test;

import com.ardverk.dht.ArdverkDHT;
import com.ardverk.dht.ArdverkUtils;
import com.ardverk.dht.KUID;
import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.config.DefaultLookupConfig;
import com.ardverk.dht.config.LookupConfig;
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
            
            // Sort the DHTs by their XOR distance to the given lookupId.
            TreeSet<KUID> expected = new TreeSet<KUID>(
                    new XorComparator(lookupId));
            for (ArdverkDHT dht : dhts) {
                expected.add(dht.getLocalhost().getId());
            }
            
            ArdverkDHT first = dhts.get(0);
            
            LookupConfig config = new DefaultLookupConfig();
            config.setLookupTimeout(20L, TimeUnit.SECONDS);
            
            ArdverkFuture<NodeEntity> future 
                = first.lookup(lookupId, config);
            NodeEntity entity = future.get();
            TestCase.assertEquals(lookupId, entity.getId());
            
            Contact[] contacts = entity.getContacts();
            
            // The Contacts in the response should be in the same order
            // as our DHT instances!
            int k = first.getRouteTable().getK();
            for (int i = 0; i < k && i < contacts.length; i++) {
                KUID contactId = contacts[i].getId();
                KUID expectedId = expected.pollFirst();
                
                TestCase.assertEquals(expectedId, contactId);
            }
            
        } finally {
            IoUtils.closeAll(dhts);
        }
    }
}
