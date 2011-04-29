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
import org.ardverk.dht.message.AbstractContent;
import org.ardverk.dht.message.Content;
import org.ardverk.io.IoUtils;

public class Status extends AbstractContent implements IntegerValue, StringValue {
    
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
    public static Status valueOf(int code, String message, Content content) {
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
    public static Status conflict(Content content) {
        return new Status(Code.CONFLICT, content);
    }
    
    private final int code;
    
    private final String message;
    
    private final Content content;
    
    private byte[] payload = null;
    
    private Status(Code code, Content content) {
        this(code.value, code.name(), content);
    }
    
    private Status(int code, String message, Content content) {
        this.code = code;
        this.message = message;
        this.content = content;
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
        return payload().length;
    }

    @Override
    public InputStream getContent() throws IOException {
        return new ByteArrayInputStream(payload());
    }
    
    @Override
    public boolean isRepeatable() {
        return true;
    }

    @Override
    public boolean isStreaming() {
        return false;
    }
    
    private synchronized byte[] payload() {
        if (payload == null) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                
                MessageOutputStream out = new MessageOutputStream(baos);
                out.writeInt(code);
                out.writeString(message);
                
                if (content != null) {
                    out.writeBoolean(true);
                    out.writeContent(content);
                } else {
                    out.writeBoolean(false);
                }
                out.close();
                
                payload = baos.toByteArray();
            } catch (IOException err) {
                throw new IllegalStateException("IOException", err);
            }
        }
        
        return payload;
    }
    
    public static Status valueOf(Content content) {
        MessageInputStream in = null;
        try {
            in = new MessageInputStream(content.getContent());
            int code = in.readInt();
            String message = in.readString();
            
            Content body = null;
            if (in.readBoolean()) {
                body = in.readStreamingContent();
            }
            
            return valueOf(code, message, body);
        } catch (IOException err) {
            throw new IllegalStateException("IOException", err);
        } finally {
            IoUtils.close(in);
        }
    }
}