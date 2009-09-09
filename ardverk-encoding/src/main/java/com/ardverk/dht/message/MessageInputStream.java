package com.ardverk.dht.message;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.ardverk.coding.BencodingInputStream;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact;

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
    
    public Contact readContact() throws IOException {
        KUID contactId = readKUID();
        int instanceId = readInt();
        SocketAddress address = readSocketAddress();
        return null;
    }
    
    public Message readMessage() throws IOException {
        OpCode opcode = readEnum(OpCode.class);
        int version = readUnsignedByte();
        if (version != MessageUtils.VERSION) {
            throw new IOException("version=" + version);
        }
        
        MessageId messageId = readMessageId();
        Contact contact = readContact();
        long time = readLong();
        
        InetAddress address = readInetAddress();
        
        return null;
    }
}
