/*
 * Copyright 2009-2012 Roger Kapsi
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

package org.ardverk.dht.routing;

import java.net.InetSocketAddress;

import junit.framework.TestCase;

import org.ardverk.dht.KUID;
import org.ardverk.dht.routing.Contact.Type;
import org.junit.Test;


public class ContactTest {

    @Test
    public void equals() {
        
        // Contacts are considered equal if their KUIDs are equal.
        // Everything else such as address, type and so on don't
        // matter at all!
        
        Contact contact1 = new DefaultContact(Type.SOLICITED, 
                KUID.createRandom(20), 
                0, false, new InetSocketAddress("www.apple.com", 2000));
        
        Contact contact2 = new DefaultContact(Type.UNKNOWN, 
                contact1.getId(), 
                1, false, new InetSocketAddress("www.microsoft.com", 3000));
        
        Contact contact3 = new DefaultContact(Type.UNSOLICITED, 
                KUID.createRandom(20), 
                2, false, new InetSocketAddress("www.google.com", 4000));
        
        Contact contact4 = new Identity(contact1.getId(), 
                new InetSocketAddress(1000));
        
        TestCase.assertTrue(contact1.equals(contact2));
        TestCase.assertTrue(contact2.equals(contact1));
        TestCase.assertTrue(contact1.equals(contact4));
        TestCase.assertEquals(contact1, contact2);
        TestCase.assertEquals(contact1, contact4);
        
        TestCase.assertFalse(contact1.equals(contact3));
        TestCase.assertFalse(contact3.equals(contact1));
        TestCase.assertFalse(contact3.equals(contact4));
    }
}