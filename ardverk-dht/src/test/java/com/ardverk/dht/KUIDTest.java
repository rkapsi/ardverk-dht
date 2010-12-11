package com.ardverk.dht;

import java.util.Arrays;

import junit.framework.TestCase;

import org.ardverk.collection.KeyAnalyzer;
import org.junit.Test;

import com.ardverk.dht.utils.XorComparator;

public class KUIDTest {

    @Test
    public void length() {
        byte[] value = new byte[] { 1, 2, 3 };
        KUID contactId = KUID.create(value);
        
        TestCase.assertEquals(value.length, contactId.length());
        TestCase.assertEquals(value.length * Byte.SIZE, contactId.lengthInBits());
    }
    
    @Test
    public void isBitSet() {
        KUID contactId = KUID.create(new byte[] { 
            (byte)0xAC /* 10101100 */,
            (byte)0x69 /* 01101001 */,
        });
        
        TestCase.assertTrue(contactId.isBitSet(0));
        TestCase.assertFalse(contactId.isBitSet(1));
        TestCase.assertTrue(contactId.isBitSet(2));
        TestCase.assertFalse(contactId.isBitSet(3));
        TestCase.assertTrue(contactId.isBitSet(4));
        TestCase.assertTrue(contactId.isBitSet(5));
        TestCase.assertFalse(contactId.isBitSet(6));
        TestCase.assertFalse(contactId.isBitSet(7));
        
        TestCase.assertFalse(contactId.isBitSet(8));
        TestCase.assertTrue(contactId.isBitSet(9));
        TestCase.assertTrue(contactId.isBitSet(10));
        TestCase.assertFalse(contactId.isBitSet(11));
        TestCase.assertTrue(contactId.isBitSet(12));
        TestCase.assertFalse(contactId.isBitSet(13));
        TestCase.assertFalse(contactId.isBitSet(14));
        TestCase.assertTrue(contactId.isBitSet(15));
    }
    
    @Test
    public void bitIndex() {
        KUID id1 = KUID.create(new byte[] { 
            (byte)0xAC /* 10101100 */,
            (byte)0x69 /* 01101001 */,
        });
        
        KUID id2 = KUID.create(new byte[] { 
            (byte)0xAC /* 10101100 */,
            (byte)0x61 /* 01100001 */,
        });
        
        int bitIndex1 = id1.bitIndex(id2);
        int bitIndex2 = id2.bitIndex(id1);
        
        TestCase.assertEquals(12, bitIndex1);
        TestCase.assertEquals(12, bitIndex2);
    }
    
    @Test
    public void equalBitIndex() {
        KUID contactId = KUID.create(new byte[] { 
            (byte)0xAC /* 10101100 */,
            (byte)0x69 /* 01101001 */,
        });
        
        int bitIndex = contactId.bitIndex(contactId);
        TestCase.assertEquals(KeyAnalyzer.EQUAL_BIT_KEY, bitIndex);
    }
    
    @Test
    public void nullBitIndex() {
        KUID contactId = KUID.create(new byte[] { 0, 0 });
        
        int bitIndex = contactId.bitIndex(contactId);
        TestCase.assertEquals(KeyAnalyzer.NULL_BIT_KEY, bitIndex);
    }
    
    @Test
    public void isCloserTo() {
        for (int i = 0; i < 1000; i++) {
            KUID lookupId = KUID.createRandom(20);
            
            KUID[] contacts = new KUID[] {
                KUID.createRandom(lookupId),
                KUID.createRandom(lookupId)
            };
            
            Arrays.sort(contacts, new XorComparator(lookupId));
            
            TestCase.assertTrue(contacts[0].isCloserTo(lookupId, contacts[1]));
            TestCase.assertFalse(contacts[1].isCloserTo(lookupId, contacts[0]));
        }
    }
    
    @Test
    public void commonPrefix() {
        for (int i = 0; i < 1000; i++) {
            
            KUID a = KUID.createRandom(20);
            
            int bits = (int)(a.lengthInBits() * Math.random());
            KUID b = KUID.createWithPrefix(a, bits);
            
            int common = a.commonPrefix(b);
            TestCase.assertTrue(common + " < " + bits, common >= bits);
        }
    }
}
