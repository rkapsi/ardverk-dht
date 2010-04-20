package com.ardverk.dht.message;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.ardverk.coding.BencodingOutputStream;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact2;
import com.ardverk.dht.storage.Database.Condition;
import com.ardverk.enumeration.IntegerValue;
import com.ardverk.enumeration.StringValue;

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
        } else if (obj instanceof Contact2) {
            writeContact((Contact2)obj);
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
    
    public void writeKUID(KUID kuid) throws IOException {
        writeBytes(kuid.getBytes());
    }
    
    public void writeMessageId(MessageId messageId) throws IOException {
        writeBytes(messageId.getBytes());
    }
    
    public void writeContact(Contact2 contact) throws IOException {
        writeKUID(contact.getContactId());
        writeInt(contact.getInstanceId());
        writeSocketAddress(contact.getRemoteAddress());
    }
    
    public void writeContacts(Contact2[] contacts) throws IOException {
        writeArray(contacts);
    }
    
    public void writeCondition(Condition status) throws IOException {
        writeString(status.name());
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
        Contact2[] contacts = message.getContacts();
        
        writeInt(contacts.length);
        for (Contact2 contact : contacts) {
            writeContact(contact);
        }
    }
    
    private void writeValueRequest(ValueRequest message) throws IOException {
        writeKUID(message.getKey());
    }
    
    private void writeValueResponse(ValueResponse message) throws IOException {
        writeBytes(message.getValue());
    }
    
    private void writeStoreRequest(StoreRequest message) throws IOException {
        writeKUID(message.getKey());
        writeBytes(message.getValue());
    }
    
    private void writeStoreResponse(StoreResponse message) throws IOException {
        writeCondition(message.getStatus());
    }
}
