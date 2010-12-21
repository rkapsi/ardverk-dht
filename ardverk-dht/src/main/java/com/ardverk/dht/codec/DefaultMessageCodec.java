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

import org.ardverk.security.MessageDigestCRC32;

import com.ardverk.dht.codec.bencode.BencodeMessageCodec;
import com.ardverk.dht.message.Message;

/**
 * The {@link DefaultMessageCodec} uses some all of the 
 * following {@link MessageCodec}s.
 * 
 * @see DigestMessageCodec
 * @see CompressorMessageCodec
 * @see BencodeMessageCodec
 * @see CipherMessageCodec
 */
public class DefaultMessageCodec extends AbstractMessageCodec {

    private final MessageCodec codec;
    
    public DefaultMessageCodec() {
        this.codec = new DigestMessageCodec(
                new CompressorMessageCodec(
                    new BencodeMessageCodec()), 
                    new MessageDigestCRC32());
    }
    
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