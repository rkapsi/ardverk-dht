/*
 * Copyright 2009-2010 Roger Kapsi
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

package com.ardverk.dht.storage;

import org.ardverk.lang.Arguments;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact;

public class DefaultValueTuple extends AbstractValueTuple {
    
    private final Contact sender;
    
    private final Contact creator;
    
    private final KUID key;
    
    private final byte[] value;
    
    public DefaultValueTuple(Contact contact, KUID key, byte[] value) {
        this (contact, contact, key, value);
    }
    
    public DefaultValueTuple(Contact sender, Contact creator, 
            KUID key, byte[] value) {
        
        this.sender = Arguments.notNull(sender, "sender");
        this.creator = Arguments.notNull(
                pickCreator(sender, creator), "creator");
        
        this.key = Arguments.notNull(key, "key");
        this.value = Arguments.notNull(value, "value");
    }

    @Override
    public Contact getSender() {
        return sender;
    }
    
    @Override
    public Contact getCreator() {
        return creator;
    }
    
    @Override
    public KUID getId() {
        return key;
    }
    
    @Override
    public byte[] getValue() {
        return value;
    }

    @Override
    public int size() {
        return value != null ? value.length : 0;
    }
    
    /**
     * To save memory we're trying to re-use the {@link Contact}
     * instance if sender and creator are the same.
     */
    private static Contact pickCreator(Contact sender, Contact creator) {
        if (creator == null || sender.equals(creator)) {
            return sender;
        }
        
        return creator;
    }
}