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

package org.ardverk.dht.codec.bencode;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.ardverk.coding.BencodingInputStream;
import org.ardverk.dht.KUID;
import org.ardverk.dht.lang.IntegerValue;
import org.ardverk.dht.lang.StringValue;
import org.ardverk.dht.message.Content;
import org.ardverk.dht.message.DefaultNodeRequest;
import org.ardverk.dht.message.DefaultNodeResponse;
import org.ardverk.dht.message.DefaultPingRequest;
import org.ardverk.dht.message.DefaultPingResponse;
import org.ardverk.dht.message.DefaultStoreRequest;
import org.ardverk.dht.message.DefaultStoreResponse;
import org.ardverk.dht.message.DefaultValueRequest;
import org.ardverk.dht.message.DefaultValueResponse;
import org.ardverk.dht.message.Message;
import org.ardverk.dht.message.MessageId;
import org.ardverk.dht.message.NodeRequest;
import org.ardverk.dht.message.NodeResponse;
import org.ardverk.dht.message.PingRequest;
import org.ardverk.dht.message.PingResponse;
import org.ardverk.dht.message.StoreRequest;
import org.ardverk.dht.message.StoreResponse;
import org.ardverk.dht.message.StreamingContent;
import org.ardverk.dht.message.ValueRequest;
import org.ardverk.dht.message.ValueResponse;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.routing.DefaultContact;
import org.ardverk.dht.storage.KeyFactory;
import org.ardverk.dht.storage.ResourceId;
import org.ardverk.net.NetworkUtils;
import org.ardverk.version.Vector;
import org.ardverk.version.VectorClock;


/**
 * The {@link MessageInputStream} reads {@link Message}s from
 * a {@link BencodingInputStream}.
 */
public class MessageInputStream extends BencodingInputStream {
    
    public MessageInputStream(InputStream in) {
        super(in);
    }

    @Override
    public <T extends Enum<T>> T readEnum(Class<T> clazz) throws IOException {
        if (IntegerValue.class.isAssignableFrom(clazz)) {
            int value = readInt();
            return readEnum(clazz, "valueOf", int.class, value);
        } else if (StringValue.class.isAssignableFrom(clazz)) {
            String value = readString();
            return readEnum(clazz, "from", String.class, value);
        } else {
            return super.readEnum(clazz);
        }
    }

    private static <T extends Enum<T>> T readEnum(Class<T> clazz, 
            String method, Class<?> type, Object value) throws IOException {
        try {
            Method m = clazz.getMethod(method, type);
            return clazz.cast(m.invoke(null, value));
        } catch (SecurityException e) {
            throw new IOException("SecurityException", e);
        } catch (IllegalArgumentException e) {
            throw new IOException("IllegalArgumentException", e);
        } catch (NoSuchMethodException e) {
            throw new IOException("NoSuchMethodException", e);
        } catch (IllegalAccessException e) {
            throw new IOException("IllegalAccessException", e);
        } catch (InvocationTargetException e) {
            throw new IOException("InvocationTargetException", e);
        }
    }

    public MessageId readMessageId() throws IOException {
        return MessageId.create(readBytes());
    }
    
    public KUID readKUID() throws IOException {
        return KUID.create(readBytes());
    }
    
    public InetAddress readInetAddress() throws IOException {
        return InetAddress.getByName(readString());
    }
    
    public SocketAddress readSocketAddress() throws IOException {
        String value = readString();
        int p = value.indexOf(':');
        
        String host = value.substring(0, p);
        int port = Integer.parseInt(value.substring(++p));
        
        return NetworkUtils.createUnresolved(host, port);
    }
    
    public ResourceId readResourceId() throws IOException {
        URI uri = URI.create(readString());
        return KeyFactory.parseKey(uri);
    }
    
    public VectorClock<KUID> readVectorClock() throws IOException {
        int count = readUnsignedShort();
        if (count == 0) {
            return null;
        }
        
        long creationTime = readLong();
        Map<KUID, Vector> dst = new HashMap<KUID, Vector>(count);
        
        while (0 < count--) {
            KUID contactId = readKUID();
            Vector vector = readVector();
            
            if (!vector.isEmpty()) {
                dst.put(contactId, vector);
            }
        }
        
        return VectorClock.create(creationTime, dst);
    }
    
    private Vector readVector() throws IOException {
        long timeStamp = readLong();
        int value = readInt();
        return new Vector(timeStamp, value);
    }
    
    public Contact readSender(Contact.Type type, SocketAddress src) throws IOException {
        KUID contactId = readKUID();
        int instanceId = readInt();
        boolean invisible = readBoolean();
        SocketAddress address = readSocketAddress();
        
        return new DefaultContact(type, contactId, 
                instanceId, invisible, src, address);
    }
    
    public Contact readContact() throws IOException {
        KUID contactId = readKUID();
        SocketAddress address = readSocketAddress();
        
        return new DefaultContact(contactId, address);
    }
    
    public Contact[] readContacts() throws IOException {
        Contact[] contacts = new Contact[readInt()];
        for (int i = 0; i < contacts.length; i++) {
            contacts[i] = readContact();
        }
        return contacts;
    }
    
    public Content readStreamingContent() throws IOException {
        ContentInputStream in = readContent();
        return new StreamingContent(in);
    }
    
    public Message readMessage(SocketAddress src) throws IOException {
        int version = readUnsignedByte();
        if (version != Constants.VERSION) {
            throw new IOException("version=" + version);
        }
        
        OpCode opcode = readEnum(OpCode.class);
        MessageId messageId = readMessageId();
        Contact contact = readSender(opcode.isRequest() 
                ? Contact.Type.UNSOLICITED : Contact.Type.SOLICITED, src);
        SocketAddress address = readSocketAddress();
        
        switch (opcode) {
            case PING_REQUEST:
                return readPingRequest(messageId, contact, address);
            case PING_RESPONSE:
                return readPingResponse(messageId, contact, address);
            case FIND_NODE_REQUEST:
                return readNodeRequest(messageId, contact, address);
            case FIND_NODE_RESPONSE:
                return readNodeResponse(messageId, contact, address);
            case FIND_VALUE_REQUEST:
                return readValueRequest(messageId, contact, address);
            case FIND_VALUE_RESPONSE:
                return readValueResponse(messageId, contact, address);
            case STORE_REQUEST:
                return readStoreRequest(messageId, contact, address);
            case STORE_RESPONSE:
                return readStoreResponse(messageId, contact, address);
            default:
                throw new IllegalArgumentException("opcode=" + opcode);
        }
    }
    
    private PingRequest readPingRequest(MessageId messageId, 
            Contact contact, SocketAddress address) throws IOException {
        return new DefaultPingRequest(messageId, contact, address);
    }
    
    private PingResponse readPingResponse(MessageId messageId, 
            Contact contact, SocketAddress address) throws IOException {
        return new DefaultPingResponse(messageId, contact, address);
    }
    
    private NodeRequest readNodeRequest(MessageId messageId, 
            Contact contact, SocketAddress address) throws IOException {
        KUID lookupId = readKUID();
        return new DefaultNodeRequest(messageId, contact, address, lookupId);
    }
    
    private NodeResponse readNodeResponse(MessageId messageId, 
            Contact contact, SocketAddress address) throws IOException {
        
        Contact[] contacts = readContacts();
        return new DefaultNodeResponse(messageId, contact, address, contacts);
    }
    
    private ValueRequest readValueRequest(MessageId messageId, 
            Contact contact, SocketAddress address) throws IOException {
        
        ResourceId resourceId = readResourceId();
        return new DefaultValueRequest(messageId, contact, address, resourceId);
    }
    
    private ValueResponse readValueResponse(MessageId messageId, 
            Contact contact, SocketAddress address) throws IOException {
        
        Content content = readStreamingContent();
        return new DefaultValueResponse(messageId, contact, address, content);
    }
    
    private StoreRequest readStoreRequest(MessageId messageId, 
            Contact contact, SocketAddress address) throws IOException {
        
        ResourceId resourceId = readResourceId();
        Content content = readStreamingContent();
        return new DefaultStoreRequest(messageId, contact, 
                address, resourceId, content);
    }
    
    private StoreResponse readStoreResponse(MessageId messageId, 
            Contact contact, SocketAddress address) throws IOException {
        Content content = readStreamingContent();
        return new DefaultStoreResponse(messageId, contact, address, content);
    }
}