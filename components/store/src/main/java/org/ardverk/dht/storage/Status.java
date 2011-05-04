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

import org.ardverk.dht.codec.bencode.MessageInputStream;
import org.ardverk.dht.codec.bencode.MessageOutputStream;
import org.ardverk.dht.lang.IntegerValue;
import org.ardverk.dht.lang.StringValue;
import org.ardverk.dht.message.StoreResponse;
import org.ardverk.io.SequenceInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Status extends SimpleValue implements IntegerValue, StringValue {
    
    private static final Logger LOG = LoggerFactory.getLogger(Status.class);
    
    private static enum Code {
        SUCCESS(100),
        FAILURE(200),
        CONFLICT(201);
        
        private final int value;
        
        private Code(int value) {
            this.value = value;
        }
    }
    
    /**
     * A generic {@link Status} if the STORE operation completed successfully.
     */
    public static final Status SUCCESS = new Status(Code.SUCCESS, null);
    
    /**
     * A generic {@link Status} if the STORE operation failed.
     */
    public static final Status FAILURE = new Status(Code.FAILURE, null);
    
    /**
     * Factory method to create {@link Status}.
     */
    public static Status valueOf(int code, String message, Value content) {
        if (content == null) {
            switch (code) {
                case 100:
                    if (SUCCESS.stringValue().equalsIgnoreCase(message)) {
                        return SUCCESS;
                    }
                    break;
                case 200:
                    if (FAILURE.stringValue().equalsIgnoreCase(message)) {
                        return FAILURE;
                    }
                    break;
            }
        }
        
        return new Status(code, message, content);
    }
    
    /**
     * Creates a {@link Status} for the case there was a conflict.
     */
    public static Status conflict(Value content) {
        return new Status(Code.CONFLICT, content);
    }
    
    private final int code;
    
    private final String message;
    
    private final Value value;
    
    private byte[] payload = null;
    
    private Status(Code code, Value value) {
        this(code.value, code.name(), value);
    }
    
    private Status(int code, String message, Value value) {
        super(ValueType.STATUS);
        this.code = code;
        this.message = message;
        this.value = value;
    }
    
    @Override
    public int intValue() {
        return code;
    }
    
    @Override
    public String stringValue() {
        return message;
    }
    
    @Override
    public int hashCode() {
        return code;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof Status)) {
            return false;
        }
        
        Status other = (Status)o;
        return code == other.intValue();
    }
    
    @Override
    public String toString() {
        return code + ", " + message;
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
                
                out.writeInt(code);
                out.writeString(message);
                out.writeBoolean(value != null);
                
                out.close();
                
                payload = baos.toByteArray();
            } catch (IOException err) {
                throw new IllegalStateException("IOException", err);
            }
        }
        
        return payload;
    }
    
    public static Status valueOf(MessageInputStream in) throws IOException {
        int code = in.readInt();
        String message = in.readString();
        
        Value body = null;
        if (in.readBoolean()) {
            body = in.readValue();
        }
        
        return valueOf(code, message, body);
    }
    
    public static boolean isSuccess(StoreResponse... responses) {
        for (StoreResponse response : responses) {
            
            SimpleValue value = null;
            try {
                value = SimpleValue.valueOf(response.getValue());
            } catch (IOException err) {
                LOG.error("IOException", err);
            }
            
            if (!(value instanceof Status)) {
                return false;
            }
            
            Status status = (Status)value;
            if (!status.equals(Status.SUCCESS)) {
                return false;
            }
        }
        
        return true;
    }
}