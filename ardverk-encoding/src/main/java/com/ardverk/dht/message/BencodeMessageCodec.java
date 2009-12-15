package com.ardverk.dht.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
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
    
    private static int COUNTER = 0;
    
    @Override
    public byte[] encode(Message message)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MessageOutputStream out = new MessageOutputStream(baos);
        
        out.writeMessage(message);
        out.close();
        
        /*FileOutputStream fos = new FileOutputStream("out-" + (COUNTER++) + ".dat");
        fos.write(baos.toByteArray());
        fos.close();*/
        
        return baos.toByteArray();
    }
}
