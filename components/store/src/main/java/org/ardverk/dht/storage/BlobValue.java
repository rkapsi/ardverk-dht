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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Collections;
import java.util.Map;

import org.ardverk.dht.KUID;
import org.ardverk.dht.codec.bencode.MessageInputStream;
import org.ardverk.dht.codec.bencode.MessageOutputStream;
import org.ardverk.dht.routing.Contact;
import org.ardverk.version.VectorClock;

public class BlobValue extends SimpleValue {
    
    private final Contact creator;
    
    private final VectorClock<KUID> clock;
    
    private final Map<String, String> properties;
    
    private final Value value;
    
    private byte[] payload = null;
    
    public BlobValue(Contact creator, VectorClock<KUID> clock, Value value) {
        this(creator, clock, Collections.<String, String>emptyMap(), value);
    }
    
    public BlobValue(Contact creator, VectorClock<KUID> clock, 
            Map<String, String> properties, Value value) {
        super(ValueType.BLOB);
        
        this.creator = creator;
        this.clock = clock;
        this.properties = properties;
        this.value = value;
    }
    
    public Contact getCreator() {
        return creator;
    }
    
    public VectorClock<KUID> getVectorClock() {
        return clock;
    }
    
    public Value getValue() {
        return value;
    }
    
    public long size() {
        return value.getContentLength();
    }
    
    public boolean isEmpty() {
        return size() == 0;
    }
    
    @Override
    public String toString() {
        return value.toString();
    }
    
    @Override
    public long getContentLength() {
        long contentLength = payload().length;
        if (value != null) {
            contentLength += value.getContentLength();
        }
        return contentLength;
    }

    @Override
    public InputStream getContent() throws IOException {
        InputStream in = new ByteArrayInputStream(payload());
        
        if (value != null) {
            //in = new SequenceInputStream(in, value.getContent());
            in = new SequenceInputStream(in, value.getContent());
        }
        
        return in;
    }
    
    @Override
    public boolean isRepeatable() {
        if (value != null) {
            return value.isRepeatable();
        }
        return true;
    }

    @Override
    public boolean isStreaming() {
        if (value != null) {
            return value.isStreaming();
        }
        return false;
    }

    private synchronized byte[] payload() {
        if (payload == null) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                MessageOutputStream out = new MessageOutputStream(baos);
                
                writeHeader(out);
                out.writeContact(creator);
                out.writeVectorClock(clock);
                out.writeMap(properties);
                
                long contentLength = 0L;
                if (value != null) {
                    contentLength = value.getContentLength();
                }
                out.writeContentLength(contentLength);
                
                out.close();
                
                payload = baos.toByteArray();
                
            } catch (IOException err) {
                throw new IllegalStateException("IOException", err);
            }
        }
        return payload;
    }
    
    @SuppressWarnings("unchecked")
    public static BlobValue valueOf(MessageInputStream in) throws IOException {
        Contact creator = in.readContact();
        VectorClock<KUID> clock = in.readVectorClock();
        Map<String, String> properties = (Map<String, String>)in.readMap();
        
        Value value = null;
        if (in.readBoolean()) {
            value = ByteArrayValue.valueOf(in.readValue());
        }
        
        return new BlobValue(creator, clock, properties, value);
    }
}