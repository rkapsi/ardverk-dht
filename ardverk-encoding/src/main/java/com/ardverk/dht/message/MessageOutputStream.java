package com.ardverk.dht.message;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.ardverk.coding.BencodingOutputStream;

import com.ardverk.dht.KUID;
import com.ardverk.dht.io.SessionContext;
import com.ardverk.dht.routing.Contact;

class MessageOutputStream extends BencodingOutputStream {

    public MessageOutputStream(OutputStream out, String charset) {
        super(out, charset);
    }

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
        //} else if (obj instanceof Message) {
        //    writeMessage((Message)obj);
        } else {
            super.writeCustom(obj);
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
    
    public void writeMessage(SessionContext context, 
            Message message) throws IOException {
        
        writeEnum(message.getOpCode());
        writeMessageId(message.getMessageId());
        
        writeSocketAddress(context.getRemoteAddress());
        
        writeContact(message.getContact());
    }
}
