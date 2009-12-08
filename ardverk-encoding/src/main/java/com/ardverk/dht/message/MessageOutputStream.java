package com.ardverk.dht.message;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.ardverk.coding.BencodingOutputStream;

import com.ardverk.dht.KUID;
import com.ardverk.dht.io.session.SessionContext;
import com.ardverk.dht.routing.Contact;
import com.ardverk.enumeration.IntegerValue;
import com.ardverk.enumeration.StringValue;

class MessageOutputStream extends BencodingOutputStream {

    private final SessionContext context;
    
    public MessageOutputStream(OutputStream out, SessionContext context) {
        super(out);
        
        if (context == null) {
            throw new NullPointerException("context");
        }
        
        this.context = context;
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
    
    public void writeKUID(KUID kuid) throws IOException {
        writeBytes(kuid.getBytes());
    }
    
    public void writeMessageId(MessageId messageId) throws IOException {
        writeBytes(messageId.getBytes());
    }
    
    public void writeContact(Contact contact) throws IOException {
        writeKUID(contact.getContactId());
        writeInt(contact.getInstanceId());
        writeSocketAddress(contact.getRemoteAddress());
    }
    
    public void writeMessage(Message message) throws IOException {
        
        writeByte(MessageUtils.VERSION);
        writeEnum(OpCode.valueOf(message));
        writeMessageId(message.getMessageId());
        
        // Write the source and destination
        writeContact(message.getContact());
        writeSocketAddress(message.getAddress());
        
        if (message instanceof PingRequest) {
            writePingRequest((PingRequest)message);
        } else if (message instanceof PingResponse) {
            writePingResponse((PingResponse)message);
        }
    }
    
    private void writePingRequest(PingRequest message) throws IOException {
    }
    
    private void writePingResponse(PingResponse message) throws IOException {
    }
}
