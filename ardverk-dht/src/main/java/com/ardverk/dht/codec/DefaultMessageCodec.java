package com.ardverk.dht.codec;

import java.io.IOException;
import java.net.SocketAddress;

import org.ardverk.security.MessageDigestCRC32;

import com.ardverk.dht.message.BencodeMessageCodec;
import com.ardverk.dht.message.Message;

public class DefaultMessageCodec extends AbstractMessageCodec {

    private final MessageCodec codec;
    
    public DefaultMessageCodec(String secretKey, String initVector) {
        this.codec = new DigestMessageCodec(
                new CipherMessageCodec(
                    new CompressorMessageCodec(
                        new BencodeMessageCodec()), 
                        secretKey, initVector), 
                        new MessageDigestCRC32());
    }
    
    @Override
    public byte[] encode(Message message) throws IOException {
        return codec.encode(message);
    }

    @Override
    public Message decode(SocketAddress src, byte[] data, int offset, int length)
            throws IOException {
        return codec.decode(src, data, offset, length);
    }
}
