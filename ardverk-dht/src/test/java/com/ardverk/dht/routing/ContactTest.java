/*
 * Copyright 2010 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ardverk.dht.routing;

import java.net.InetSocketAddress;

import junit.framework.TestCase;

import org.junit.Test;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact.Type;

public class ContactTest {

    @Test
    public void equals() {
        
        // Contacts are considered equal if their KUIDs are equal.
        // Everything else such as address, type and so on don't
        // matter at all!
        
        Contact contact1 = new Contact(Type.SOLICITED, 
                KUID.createRandom(20), 
                0, new InetSocketAddress("www.apple.com", 2000));
        
        Contact contact2 = new Contact(Type.UNKNOWN, 
                contact1.getId(), 
                1, new InetSocketAddress("www.microsoft.com", 3000));
        
        Contact contact3 = new Contact(Type.UNSOLICITED, 
                KUID.createRandom(20), 
                2, new InetSocketAddress("www.google.com", 4000));
        
        TestCase.assertTrue(contact1.equals(contact2));
        TestCase.assertTrue(contact2.equals(contact1));
        TestCase.assertEquals(contact1, contact2);
        
        TestCase.assertFalse(contact1.equals(contact3));
        TestCase.assertFalse(contact3.equals(contact1));
    }
}