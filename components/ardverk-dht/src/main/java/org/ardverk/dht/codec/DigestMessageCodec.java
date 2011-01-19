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

package org.ardverk.dht.codec;

import java.io.IOException;
import java.net.SocketAddress;
import java.security.MessageDigest;

import org.ardverk.dht.message.Message;


/**
 * The {@link DigestMessageCodec} adds and checks a {@link MessageDigest} to
 * every outgoing and incoming {@link Message}.
 * 
 * @see MessageDigest
 */
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
                throw new IOException("Checksum Error in Message");
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