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

package com.ardverk.dht.codec.bencode;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.SocketAddress;

import org.ardverk.coding.BencodingInputStream;
import org.ardverk.net.NetworkUtils;

import com.ardverk.dht.KUID;
import com.ardverk.dht.lang.IntegerValue;
import com.ardverk.dht.lang.StringValue;
import com.ardverk.dht.message.DefaultNodeRequest;
import com.ardverk.dht.message.DefaultNodeResponse;
import com.ardverk.dht.message.DefaultPingRequest;
import com.ardverk.dht.message.DefaultPingResponse;
import com.ardverk.dht.message.DefaultStoreRequest;
import com.ardverk.dht.message.DefaultStoreResponse;
import com.ardverk.dht.message.DefaultValueRequest;
import com.ardverk.dht.message.DefaultValueResponse;
import com.ardverk.dht.message.Message;
import com.ardverk.dht.message.MessageId;
import com.ardverk.dht.message.NodeRequest;
import com.ardverk.dht.message.NodeResponse;
import com.ardverk.dht.message.PingRequest;
import com.ardverk.dht.message.PingResponse;
import com.ardverk.dht.message.StoreRequest;
import com.ardverk.dht.message.StoreResponse;
import com.ardverk.dht.message.ValueRequest;
import com.ardverk.dht.message.ValueResponse;
import com.ardverk.dht.routing.DefaultContact;
import com.ardverk.dht.routing.IContact;
import com.ardverk.dht.routing.IContact.Type;
import com.ardverk.dht.storage.Database.Condition;
import com.ardverk.dht.storage.DefaultCondition;
import com.ardverk.dht.storage.DefaultValueTuple;
import com.ardverk.dht.storage.ValueTuple;

class MessageInputStream extends BencodingInputStream {
    
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
    
    public DefaultContact readContact(Contact.Type type, SocketAddress src) throws IOException {
        KUID contactId = readKUID();
        int instanceId = readInt();
        SocketAddress address = readSocketAddress();
        
        if (type == IContact.Type.UNKNOWN) {
            src = address;
        }
        
        return new DefaultContact(type, contactId, 
                instanceId, src, address);
    }
    
    public DefaultContact[] readContacts(Contact.Type type, SocketAddress src) throws IOException {
        DefaultContact[] contacts = new DefaultContact[readInt()];
        for (int i = 0; i < contacts.length; i++) {
            contacts[i] = readContact(type, src);
        }
        return contacts;
    }
    
    public Condition readCondition() throws IOException {
        int code = readInt();
        String value = readString();
        return DefaultCondition.valueOf(code, value);
    }
    
    public ValueTuple readValueTuple(DefaultContact contact, 
            SocketAddress address) throws IOException {
        DefaultContact creator = readContact(Type.UNKNOWN, address);
        
        KUID key = readKUID();
        byte[] value = readBytes();
        
        return new DefaultValueTuple(contact, creator, key, value);
    }
    
    public Message readMessage(SocketAddress src) throws IOException {
        int version = readUnsignedByte();
        if (version != MessageUtils.VERSION) {
            throw new IOException("version=" + version);
        }
        
        OpCode opcode = readEnum(OpCode.class);
        MessageId messageId = readMessageId();
        DefaultContact contact = readContact(opcode.isRequest() 
                ? IContact.Type.UNSOLICITED : IContact.Type.SOLICITED, src);
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
            DefaultContact contact, SocketAddress address) throws IOException {
        return new DefaultPingRequest(messageId, contact, address);
    }
    
    private PingResponse readPingResponse(MessageId messageId, 
            DefaultContact contact, SocketAddress address) throws IOException {
        return new DefaultPingResponse(messageId, contact, address);
    }
    
    private NodeRequest readNodeRequest(MessageId messageId, 
            DefaultContact contact, SocketAddress address) throws IOException {
        KUID key = readKUID();
        return new DefaultNodeRequest(messageId, contact, address, key);
    }
    
    private NodeResponse readNodeResponse(MessageId messageId, 
            DefaultContact contact, SocketAddress address) throws IOException {
        
        DefaultContact[] contacts = readContacts(IContact.Type.UNKNOWN, address);
        return new DefaultNodeResponse(messageId, contact, address, contacts);
    }
    
    private ValueRequest readValueRequest(MessageId messageId, 
            DefaultContact contact, SocketAddress address) throws IOException {
        
        KUID key = readKUID();
        return new DefaultValueRequest(messageId, contact, address, key);
    }
    
    private ValueResponse readValueResponse(MessageId messageId, 
            DefaultContact contact, SocketAddress address) throws IOException {
        
        ValueTuple value = readValueTuple(contact, address);
        return new DefaultValueResponse(messageId, contact, address, value);
    }
    
    private StoreRequest readStoreRequest(MessageId messageId, 
            DefaultContact contact, SocketAddress address) throws IOException {
        
        ValueTuple value = readValueTuple(contact, address);
        return new DefaultStoreRequest(messageId, contact, address, value);
    }
    
    private StoreResponse readStoreResponse(MessageId messageId, 
            DefaultContact contact, SocketAddress address) throws IOException {
        Condition condition = readCondition();
        return new DefaultStoreResponse(messageId, contact, address, condition);
    }
}