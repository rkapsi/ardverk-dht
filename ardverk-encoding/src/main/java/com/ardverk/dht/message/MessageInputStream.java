package com.ardverk.dht.message;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.ardverk.coding.BencodingInputStream;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.DefaultContact;

class MessageInputStream extends BencodingInputStream {

    public MessageInputStream(InputStream in) {
        super(in);
    }

    public MessageId readMessageId() throws IOException {
        return new MessageId(readBytes());
    }
    
    public KUID readKUID() throws IOException {
        return new KUID(readBytes());
    }
    
    public InetAddress readInetAddress() throws IOException {
        return InetAddress.getByName(readString());
    }
    
    public SocketAddress readSocketAddress() throws IOException {
        String value = readString();
        int p = value.indexOf(':');
        return new InetSocketAddress(
                value.substring(0, p), 
                Integer.parseInt(value.substring(++p)));
    }
    
    public Contact readContact(Contact.Type type) throws IOException {
        KUID contactId = readKUID();
        int instanceId = readInt();
        SocketAddress address = readSocketAddress();
        return new DefaultContact(type, contactId, 
                instanceId, address, null);
    }
    
    public Message readMessage(Contact.Type type) throws IOException {
        OpCode opcode = readEnum(OpCode.class);
        int version = readUnsignedByte();
        if (version != MessageUtils.VERSION) {
            throw new IOException("version=" + version);
        }
        
        MessageId messageId = readMessageId();
        Contact contact = readContact(type);
        long time = readLong();
        
        InetAddress address = readInetAddress();
        
        return null;
    }
}
