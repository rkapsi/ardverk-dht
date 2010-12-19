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
import com.ardverk.dht.routing.IContact;

public class DefaultValueTuple extends AbstractValueTuple {
    
    private final IContact sender;
    
    private final IContact creator;
    
    private final KUID key;
    
    private final byte[] value;
    
    public DefaultValueTuple(IContact contact, KUID key, byte[] value) {
        this (contact, contact, key, value);
    }
    
    public DefaultValueTuple(IContact sender, IContact creator, 
            KUID key, byte[] value) {
        
        this.sender = Arguments.notNull(sender, "sender");
        this.creator = Arguments.notNull(
                pickCreator(sender, creator), "creator");
        
        this.key = Arguments.notNull(key, "key");
        this.value = Arguments.notNull(value, "value");
    }

    @Override
    public IContact getSender() {
        return sender;
    }
    
    @Override
    public IContact getCreator() {
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
     * To save memory we're trying to re-use the {@link IContact}
     * instance if sender and creator are the same.
     */
    private static IContact pickCreator(IContact sender, IContact creator) {
        if (creator == null || sender.equals(creator)) {
            return sender;
        }
        
        return creator;
    }
}