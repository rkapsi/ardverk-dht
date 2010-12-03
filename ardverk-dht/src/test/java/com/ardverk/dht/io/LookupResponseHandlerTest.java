package com.ardverk.dht.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.junit.Test;

import com.ardverk.dht.KUID;
import com.ardverk.dht.lang.Identifier;
import com.ardverk.dht.utils.XorComparator;

public class LookupResponseHandlerTest {

    @Test
    public void isCloserTo() {
        KUID lookupId = KUID.createRandom(20);
        
        List<KUID> contacts = new ArrayList<KUID>();
        for (int i = 0; i < 100; i++) {
            KUID contact = KUID.createRandom(lookupId);
            contacts.add(contact);
        }
        
        Collections.sort(contacts, new XorComparator(lookupId));
        
        for (int i = 1; i < contacts.size(); i++) {
            TestCase.assertTrue(contacts.get(i-1).isCloserTo(
                    lookupId, contacts.get(i)));
        }
        
        TestCase.assertTrue(contacts.get(0).isCloserTo(
                    lookupId, contacts.get(contacts.size()-1)));
    }
    
    @Test
    public void pollFirst() {
        KUID lookupId = KUID.createRandom(20);
        Comparator<Identifier> comparator 
            = new XorComparator(lookupId);
        
        List<KUID> contacts1 = new ArrayList<KUID>();
        TreeSet<KUID> contacts2 = new TreeSet<KUID>(comparator);
        
        for (int i = 0; i < 100; i++) {
            KUID contact = KUID.createRandom(lookupId);
            contacts1.add(contact);
            contacts2.add(contact);
        }
        
        Collections.sort(contacts1, comparator);
        
        KUID[] contacts3 = contacts2.toArray(new KUID[0]);
        
        // The List, Set and Array should be in the same order!
        
        TestCase.assertEquals(contacts1.size(), contacts2.size());
        TestCase.assertEquals(contacts1.size(), contacts3.length);
        
        for (int i = 0; i < contacts1.size(); i++) {
            KUID contact1 = contacts1.get(i);
            KUID contact2 = contacts2.pollFirst();
            KUID contact3 = contacts3[i];
            
            TestCase.assertEquals(contact1, contact2);
            TestCase.assertEquals(contact1, contact3);
            TestCase.assertEquals(contact2, contact3);
        }
    }
    
    @Test
    public void pollFirst2() {
        KUID lookupId = KUID.createRandom(20);
        Comparator<Identifier> comparator 
            = new XorComparator(lookupId);
        
        List<KUID> contacts1 = new ArrayList<KUID>();
        TreeMap<KUID, KUID> contacts2 = new TreeMap<KUID, KUID>(comparator);
        
        for (int i = 0; i < 100; i++) {
            KUID contact = KUID.createRandom(lookupId);
            contacts1.add(contact);
            contacts2.put(contact, contact);
        }
        
        Collections.sort(contacts1, comparator);
        
        KUID[] contacts3 = contacts2.values().toArray(new KUID[0]);
        
        // The List, Set and Array should be in the same order!
        
        TestCase.assertEquals(contacts1.size(), contacts2.size());
        TestCase.assertEquals(contacts1.size(), contacts3.length);
        
        for (int i = 0; i < contacts1.size(); i++) {
            KUID contact1 = contacts1.get(i);
            KUID contact2 = contacts2.pollFirstEntry().getValue();
            KUID contact3 = contacts3[i];
            
            TestCase.assertEquals(contact1, contact2);
            TestCase.assertEquals(contact1, contact3);
            TestCase.assertEquals(contact2, contact3);
        }
    }
}
