package com.ardverk.dht.codec;

import java.io.IOException;
import java.net.SocketAddress;
import java.security.MessageDigest;

import com.ardverk.dht.message.Message;

public class DigestMessageCodec extends AbstractMessageCodec {

    private final MessageCodec codec;
    
    private final MessageDigest messageDigest;
    
    public DigestMessageCodec(MessageCodec codec, MessageDigest messageDigest) {
        this.codec = codec;
        this.messageDigest = messageDigest;
    }

    @Override
    public byte[] encode(Message message) throws IOException {
        byte[] msg = codec.encode(message);
        byte[] digest = digest(msg);
        
        byte[] dst = new byte[msg.length + digest.length];
        System.arraycopy(msg, 0, dst, 0, msg.length);
        System.arraycopy(digest, 0, dst, msg.length, digest.length);
        return dst;
    }

    @Override
    public Message decode(SocketAddress src, byte[] data, int offset, int length)
            throws IOException {
        
        int payload = length - messageDigest.getDigestLength();
        byte[] digest = digest(data, offset, payload);
        
        for (int i = 0; i < digest.length; i++) {
            if (digest[i] != data[offset + payload + i]) {
                throw new IOException();
            }
        }
            
        return codec.decode(src, data, offset, payload);
    }
    
    private byte[] digest(byte[] data) throws IOException {
        return digest(data, 0, data.length);
    }
    
    private synchronized byte[] digest(byte[] data, 
            int offset, int length) throws IOException {
        messageDigest.reset();
        messageDigest.update(data, offset, length);
        return messageDigest.digest();
    }
}
