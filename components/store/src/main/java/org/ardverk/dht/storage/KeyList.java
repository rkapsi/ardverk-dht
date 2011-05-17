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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import org.ardverk.collection.CollectionUtils;
import org.ardverk.dht.codec.bencode.MessageOutputStream;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.dht.storage.io.ValueInputStream;
import org.ardverk.io.InputOutputStream;

public class KeyList extends SimpleValue {

    private final Key[] keys;
    
    public KeyList(Collection<? extends Key> c) {
        this(CollectionUtils.toArray(c, Key.class));
    }
    
    public KeyList(Key[] keys) {
        super(ValueType.KEY_LIST);
        this.keys = keys;
    }

    public Key[] getKeys() {
        return keys;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        int index = 0;
        for (Key key : keys) {
            sb.append(index++).append(") ").append(key).append("\n");
        }
        
        return sb.toString();
    }
    
    @Override
    public InputStream getContent() throws IOException {
        return new InputOutputStream() {
            @Override
            protected void produce(OutputStream out) throws IOException {
                MessageOutputStream mos = new MessageOutputStream(out);
                
                writeHeader(mos);
                mos.writeArray(keys);
                
                mos.close();
            }
        };
    }
    
    @Override
    public boolean isRepeatable() {
        return true;
    }

    @Override
    public boolean isStreaming() {
        return false;
    }
    
    public static KeyList valueOf(InputStream in) throws IOException {
        ValueInputStream vis = new ValueInputStream(in);
        Key[] keys = vis.readKeys();
        return new KeyList(keys);
    }
}