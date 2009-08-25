package com.ardverk.dht.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.ardverk.dht.io.SessionContext;

public class BencodeMessageCodec extends MessageCodec {

    public static final String NAME = "bencode";

    public BencodeMessageCodec() {
        super(NAME);
    }

    @Override
    public Message decode(SessionContext context, byte[] data)
            throws IOException {
        BencodingInputStream in = new BencodingInputStream(
                new ByteArrayInputStream(data));
        
        OpCode opcode = OpCode.valueOf((String)in.readObject());
        //MessageId messageId = MessageFactory.
        return null;
    }
    
    @Override
    public byte[] encode(SessionContext context, Message message)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BencodingOutputStream out = new BencodingOutputStream(baos);
        
        out.writeObject(message.getOpCode().name());
        out.writeObject(message.getMessageId().getBytes());
        
        out.writeObject(context.getRemoteAddress().getAddress().getAddress());
        
        out.close();
        return baos.toByteArray();
    }
}
