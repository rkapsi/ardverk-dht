package org.ardverk.dht.storage;

import junit.framework.TestCase;

import org.ardverk.dht.KUID;
import org.junit.Test;

public class DefaultResourceFactoryTest {

    @Test
    public void equals() {
        KUID valueId1 = KUID.createRandom(20);
        Resource resource1 = DefaultResourceFactory.valueOf(valueId1);
        Resource resource2 = DefaultResourceFactory.valueOf(valueId1);
        
        KUID valueId2 = KUID.createRandom(20);
        Resource resource3 = DefaultResourceFactory.valueOf(valueId2);
        
        TestCase.assertEquals(resource1, resource2);
        TestCase.assertFalse(resource1.equals(resource3));
    }
}
