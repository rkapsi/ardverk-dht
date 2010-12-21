/*
 * Copyright 2009-2010 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ardverk.dht.codec;

import java.io.IOException;
import java.net.SocketAddress;

import org.ardverk.io.Compressor;
import org.ardverk.io.ZlibCompressor;

import com.ardverk.dht.message.Message;

/**
 * The {@link CompressorMessageCodec} compresses and decompresses all
 * incoming and outgoing {@link Message}s.
 * 
 * @see Compressor
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