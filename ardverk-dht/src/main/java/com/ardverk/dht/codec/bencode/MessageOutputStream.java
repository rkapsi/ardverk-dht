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
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.ardverk.coding.BencodingOutputStream;

import com.ardverk.dht.KUID;
import com.ardverk.dht.lang.IntegerValue;
import com.ardverk.dht.lang.StringValue;
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
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.storage.Database.Condition;
import com.ardverk.dht.storage.ValueTuple;

class MessageOutputStream extends BencodingOutputStream {
    
    public MessageOutputStream(OutputStream out) {
        super(out);
    }

    @Override
    protected void writeCustom(Object obj) throws IOException {
        if (obj instanceof InetAddress) {
            writeInetAddress((InetAddress)obj);
        } else if (obj instanceof SocketAddress) {
            writeSocketAddress((SocketAddress)obj);
        } else if (obj instanceof KUID) {
            writeKUID((KUID)obj);
        } else if (obj instanceof MessageId) {
            writeMessageId((MessageId)obj);
        } else if (obj instanceof Contact) {
            writeContact((Contact)obj);
        } else if (obj instanceof Message) {
            writeMessage((Message)obj);
        } else {
            super.writeCustom(obj);
        }
    }
    
    @Override
    public void writeEnum(Enum<?> value) throws IOException {
        if (value instanceof IntegerValue) {
            writeInt(((IntegerValue)value).intValue());
        } else if (value instanceof StringValue) {
            writeString(((StringValue)value).stringValue());
        } else {
            super.writeEnum(value);
        }
    }

    public void writeInetAddress(InetAddress address) throws IOException {
        writeString(address.getHostName());
    }
    
    public void writeSocketAddress(SocketAddress sa) throws IOException {
        //InetSocketAddress isa = NetworkUtils.getResolved(sa);
        InetSocketAddress isa = (InetSocketAddress)sa;
        writeString(isa.getHostName() + ":" + isa.getPort());
    }
    
    public void writeKUID(KUID kuid) throws IOException {
        writeBytes(kuid.getBytes());
    }
    
    public void writeMessageId(MessageId messageId) throws IOException {
        writeBytes(messageId.getBytes());
    }
    
    public void writeContact(Contact contact) throws IOException {
        writeKUID(contact.getId());
        writeInt(contact.getInstanceId());
        writeSocketAddress(contact.getRemoteAddress());
    }
    
    public void writeContacts(Contact[] contacts) throws IOException {
        writeArray(contacts);
    }
    
    public void writeCondition(Condition condition) throws IOException {
        writeString(condition.stringValue());
    }
    
    public void writeValueTuple(ValueTuple tuple) throws IOException {
        writeContact(tuple.getCreator());
        
        writeKUID(tuple.getId());
        writeBytes(tuple.getValue());
    }
    
    public void writeMessage(Message message) throws IOException {
        
        writeByte(MessageUtils.VERSION);
        
        OpCode opcode = OpCode.valueOf(message);
        writeEnum(opcode);
        writeMessageId(message.getMessageId());
        
        // Write the source and destination
        writeContact(message.getContact());
        writeSocketAddress(message.getAddress());
        
        switch (opcode) {
            case PING_REQUEST:
                writePingRequest((PingRequest)message);
                break;
            case PING_RESPONSE:
                writePingResponse((PingResponse)message);
                break;
            case FIND_NODE_REQUEST:
                writeNodeRequest((NodeRequest)message);
                break;
            case FIND_NODE_RESPONSE:
                writeNodeResponse((NodeResponse)message);
                break;
            case FIND_VALUE_REQUEST:
                writeValueRequest((ValueRequest)message);
                break;
            case FIND_VALUE_RESPONSE:
                writeValueResponse((ValueResponse)message);
                break;
            case STORE_REQUEST:
                writeStoreRequest((StoreRequest)message);
                break;
            case STORE_RESPONSE:
                writeStoreResponse((StoreResponse)message);
                break;
            default:
                throw new IllegalArgumentException("opcode=" + opcode);
        }
    }
    
    private void writePingRequest(PingRequest message) throws IOException {
    }
    
    private void writePingResponse(PingResponse message) throws IOException {
    }
    
    private void writeNodeRequest(NodeRequest message) throws IOException {
        writeKUID(message.getKey());
    }
    
    private void writeNodeResponse(NodeResponse message) throws IOException {
        Contact[] contacts = message.getContacts();
        
        writeInt(contacts.length);
        for (Contact contact : contacts) {
            writeContact(contact);
        }
    }
    
    private void writeValueRequest(ValueRequest message) throws IOException {
        writeKUID(message.getKey());
    }
    
    private void writeValueResponse(ValueResponse message) throws IOException {
        writeValueTuple(message.getValueTuple());
    }
    
    private void writeStoreRequest(StoreRequest message) throws IOException {
        writeValueTuple(message.getValueTuple());
    }
    
    private void writeStoreResponse(StoreResponse message) throws IOException {
        writeCondition(message.getCondition());
    }
}