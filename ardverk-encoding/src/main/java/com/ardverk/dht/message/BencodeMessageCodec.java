package com.ardverk.dht.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketAddress;

import org.ardverk.lang.NumberUtils;

public class BencodeMessageCodec extends AbstractMessageCodec {
    
    private static final int DEFAULT_MESSAGE_SIZE = 1024;
    
    private final int messageSize;
    
    public BencodeMessageCodec() {
        this(DEFAULT_MESSAGE_SIZE);
    }
    
    public BencodeMessageCodec(int messageSize) {
        this.messageSize = NumberUtils.nextPowOfTwo(messageSize);
    }
    
    @Override
    public Message decode(SocketAddress src, byte[] data, int offset, int length)
            throws IOException {
        MessageInputStream in = new MessageInputStream(
                new ByteArrayInputStream(data, offset, length));
        
        return in.readMessage(src);
    }
    
    @Override
    public byte[] encode(Message message)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(messageSize);
        MessageOutputStream out = new MessageOutputStream(baos);
        
        out.writeMessage(message);
        out.close();
        
        return baos.toByteArray();
    }
}
