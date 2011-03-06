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
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;

import org.ardverk.coding.BencodingOutputStream;
import org.ardverk.dht.KUID;
import org.ardverk.dht.lang.IntegerValue;
import org.ardverk.dht.lang.StringValue;
import org.ardverk.dht.message.Message;
import org.ardverk.dht.message.MessageId;
import org.ardverk.dht.message.NodeRequest;
import org.ardverk.dht.message.NodeResponse;
import org.ardverk.dht.message.PingRequest;
import org.ardverk.dht.message.PingResponse;
import org.ardverk.dht.message.StoreRequest;
import org.ardverk.dht.message.StoreResponse;
import org.ardverk.dht.message.ValueRequest;
import org.ardverk.dht.message.ValueResponse;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.storage.Database.Condition;
import org.ardverk.dht.storage.Descriptor;
import org.ardverk.dht.storage.Resource;
import org.ardverk.dht.storage.Value;
import org.ardverk.dht.storage.ValueTuple;
import org.ardverk.version.Vector;
import org.ardverk.version.VectorClock;


/**
 * The {@link MessageOutputStream} writes {@link Message}s to a
 * {@link BencodingOutputStream}.
 */
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
        InetSocketAddress isa = (InetSocketAddress)sa;
        writeString(isa.getHostName() + ":" + isa.getPort());
    }
    
    public void writeResource(Resource resource) throws IOException {
        writeKUID(resource.getId());
    }
    
    public void writeVectorClock(VectorClock<? extends KUID> clock) throws IOException {
        int size = 0;
        if (clock != null) {
            size = clock.size();
        }
        
        writeShort(size);
        
        if (0 < size) {
            writeLong(clock.getCreationTime());
            for (Map.Entry<? extends KUID, ? extends Vector> entry 
                    : clock.entrySet()) {
                KUID contactId = entry.getKey();
                Vector vector = entry.getValue();
                
                writeKUID(contactId);
                writeVector(vector);
            }
        }
    }
    
    private void writeVector(Vector vector) throws IOException {
        writeLong(vector.getTimeStamp());
        writeInt(vector.getValue());
    }
    
    public void writeKUID(KUID kuid) throws IOException {
        writeBytes(kuid.getBytes());
    }
    
    public void writeMessageId(MessageId messageId) throws IOException {
        writeBytes(messageId.getBytes());
    }
    
    public void writeSender(Contact contact) throws IOException {
        writeKUID(contact.getId());
        writeInt(contact.getInstanceId());
        writeBoolean(contact.isInvisible());
        writeSocketAddress(contact.getRemoteAddress());
    }
    
    public void writeContact(Contact contact) throws IOException {
        writeKUID(contact.getId());
        writeSocketAddress(contact.getRemoteAddress());
    }
    
    public void writeContacts(Contact[] contacts) throws IOException {
        writeArray(contacts);
    }
    
    public void writeCondition(Condition condition) throws IOException {
        writeInt(condition.intValue());
        writeString(condition.stringValue());
    }
    
    public void writeDescriptor(Descriptor descriptor) throws IOException {
        writeContact(descriptor.getCreator());
        writeResource(descriptor.getResource());
    }
    
    public void writeValueTuple(ValueTuple tuple) throws IOException {
        writeDescriptor(tuple.getDescriptor());
        writeVectorClock(tuple.getVectorClock());
        writeValue(tuple.getValue());
    }
    
    public void writeValue(Value value) throws IOException {
        // We assume all Values support this.
        writeBytes(value.getContentAsBytes());
    }
    
    public void writeMessage(Message message) throws IOException {
        
        writeByte(Constants.VERSION);
        
        OpCode opcode = OpCode.valueOf(message);
        writeEnum(opcode);
        writeMessageId(message.getMessageId());
        
        // Write the source and destination
        writeSender(message.getContact());
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
        writeKUID(message.getId());
    }
    
    private void writeNodeResponse(NodeResponse message) throws IOException {
        Contact[] contacts = message.getContacts();
        
        writeInt(contacts.length);
        for (Contact contact : contacts) {
            writeContact(contact);
        }
    }
    
    private void writeValueRequest(ValueRequest message) throws IOException {
        writeResource(message.getResource());
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