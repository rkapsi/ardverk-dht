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