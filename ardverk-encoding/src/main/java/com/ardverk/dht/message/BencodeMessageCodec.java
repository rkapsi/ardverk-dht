package com.ardverk.dht.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.ardverk.dht.io.session.SessionContext;

public class BencodeMessageCodec extends MessageCodec {

    public static final String NAME = "bencode";

    public BencodeMessageCodec() {
        super(NAME);
    }

    @Override
    public Message decode(SessionContext context, byte[] data)
            throws IOException {
        MessageInputStream in = new MessageInputStream(
                new ByteArrayInputStream(data), context);
        
        return in.readMessage();
    }
    
    @Override
    public byte[] encode(SessionContext context, Message message)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MessageOutputStream out = new MessageOutputStream(baos, context);
        
        out.writeMessage(message);
        out.close();
        
        return baos.toByteArray();
    }
}
