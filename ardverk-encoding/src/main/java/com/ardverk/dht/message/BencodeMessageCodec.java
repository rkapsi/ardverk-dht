package com.ardverk.dht.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketAddress;

public class BencodeMessageCodec extends MessageCodec {

    public static final String NAME = "bencode";

    public BencodeMessageCodec() {
        super(NAME);
    }

    @Override
    public Message decode(SocketAddress src, byte[] data)
            throws IOException {
        MessageInputStream in = new MessageInputStream(
                new ByteArrayInputStream(data));
        
        return in.readMessage(src);
    }
    
    @Override
    public byte[] encode(Message message)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MessageOutputStream out = new MessageOutputStream(baos);
        
        out.writeMessage(message);
        out.close();
        
        return baos.toByteArray();
    }
}
