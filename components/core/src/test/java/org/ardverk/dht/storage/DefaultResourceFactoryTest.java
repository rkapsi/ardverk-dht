package org.ardverk.dht.storage;

import junit.framework.TestCase;

import org.ardverk.dht.KUID;
import org.junit.Test;

public class DefaultResourceFactoryTest {

    @Test
    public void equals() {
        KUID valueId1 = KUID.createRandom(20);
        ResourceId resource1 = DefaultResourceIdFactory.valueOf(valueId1);
        ResourceId resource2 = DefaultResourceIdFactory.valueOf(valueId1);
        
        KUID valueId2 = KUID.createRandom(20);
        ResourceId resource3 = DefaultResourceIdFactory.valueOf(valueId2);
        
        TestCase.assertEquals(resource1, resource2);
        TestCase.assertFalse(resource1.equals(resource3));
    }
}
