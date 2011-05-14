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

import org.ardverk.dht.KUID;
import org.ardverk.dht.codec.bencode.MessageInputStream;
import org.ardverk.dht.codec.bencode.MessageOutputStream;
import org.ardverk.dht.routing.Contact;
import org.ardverk.io.InputOutputStream;
import org.ardverk.utils.StringUtils;
import org.ardverk.version.VectorClock;

public class BlobValue extends SimpleValue {

    public static final byte[] EMPTY = new byte[0];
    
    private final Contact creator;
    
    private final VectorClock<KUID> clock;
    
    private final byte[] value;
    
    public BlobValue(Contact creator, 
            VectorClock<KUID> clock, byte[] value) {
        super(ValueType.BLOB);
        this.creator = creator;
        this.clock = clock;
        this.value = value;
    }
    
    public Contact getCreator() {
        return creator;
    }
    
    public VectorClock<KUID> getVectorClock() {
        return clock;
    }
    
    public byte[] getValue() {
        return value;
    }
    
    public int size() {
        return value.length;
    }
    
    public boolean isEmpty() {
        return size() == 0;
    }
    
    public BlobValue update(Contact contact, byte[] value) {
        VectorClock<KUID> clock = this.clock;
        if (clock != null) {
            clock = clock.append(contact.getId());
        }
        
        return new BlobValue(creator, clock, value);
    }
    
    @Override
    public String toString() {
        return StringUtils.toString(value);
    }
    
    @Override
    public InputStream getContent() throws IOException {
        return new InputOutputStream() {
            @Override
            protected void produce(OutputStream out) throws IOException {
                MessageOutputStream mos = new MessageOutputStream(out);
                
                writeHeader(mos);
                mos.writeContact(creator);
                mos.writeVectorClock(clock);
                mos.writeBytes(value);
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
    
    public static BlobValue valueOf(MessageInputStream in) throws IOException {
        Contact creator = in.readContact();
        VectorClock<KUID> clock = in.readVectorClock();
        byte[] value = in.readBytes();
        return new BlobValue(creator, clock, value);
    }
}