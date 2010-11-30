package com.ardverk.dht;

import junit.framework.TestCase;

import org.junit.Test;

public class KUIDTest {

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
}
