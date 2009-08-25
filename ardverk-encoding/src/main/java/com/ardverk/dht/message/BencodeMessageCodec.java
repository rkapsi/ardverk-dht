package com.ardverk.dht.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;

public class BencodeMessageCodec extends MessageCodec {

    public static final String NAME = "bencode";

    public BencodeMessageCodec() {
        super(NAME);
    }

    @Override
    public Message decode(InetSocketAddress src, byte[] data)
            throws IOException {
        BencodingInputStream in = new BencodingInputStream(
                new ByteArrayInputStream(data));
        return null;
    }
    
    @Override
    public byte[] encode(Message message, InetSocketAddress dst)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BencodingOutputStream out = new BencodingOutputStream(baos);
        
        out.writeObject(message.getOpCode().name());
        out.writeObject(message.getMessageId().getBytes());
        out.writeObject(dst.getAddress().getAddress());
        
        out.close();
        return baos.toByteArray();
    }
}
