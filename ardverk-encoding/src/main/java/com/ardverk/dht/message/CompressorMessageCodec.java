package com.ardverk.dht.message;

import java.io.IOException;
import java.net.SocketAddress;

import org.ardverk.io.Compressor;
import org.ardverk.io.ZlibCompressor;

/**
 * 
 */
public class CompressorMessageCodec extends AbstractMessageCodec {

    private final MessageCodec codec;
    
    private final Compressor compressor;
    
    public CompressorMessageCodec(MessageCodec codec) {
        this(codec, ZlibCompressor.ZLIB);
    }
    
    public CompressorMessageCodec(MessageCodec codec, Compressor compressor) {
        this.codec = codec;
        this.compressor = compressor;
    }
    
    @Override
    public byte[] encode(Message message) throws IOException {
        byte[] data = codec.encode(message);
        return compressor.compress(data);
    }

    @Override
    public Message decode(SocketAddress src, byte[] msg, 
            int offset, int length) throws IOException {
        byte[] data = compressor.decompress(msg, offset, length);
        return codec.decode(src, data);
    }
}
