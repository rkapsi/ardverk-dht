package org.ardverk.dht.storage;

import junit.framework.TestCase;

import org.ardverk.dht.KUID;
import org.junit.Test;

public class DefaultResourceFactoryTest {

    @Test
    public void equals() {
        KUID valueId1 = KUID.createRandom(20);
        ResourceId resourceId1 = DefaultResourceId.valueOf(valueId1);
        ResourceId resourceId2 = DefaultResourceId.valueOf(valueId1);
        
        KUID valueId2 = KUID.createRandom(20);
        ResourceId resourceId3 = DefaultResourceId.valueOf(valueId2);
        
        TestCase.assertEquals(resourceId1, resourceId2);
        TestCase.assertFalse(resourceId1.equals(resourceId3));
    }
}
