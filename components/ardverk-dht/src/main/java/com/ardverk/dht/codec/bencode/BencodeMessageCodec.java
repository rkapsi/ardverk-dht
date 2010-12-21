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

package com.ardverk.dht.codec.bencode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketAddress;

import org.ardverk.io.IoUtils;
import org.ardverk.lang.NumberUtils;

import com.ardverk.dht.codec.AbstractMessageCodec;
import com.ardverk.dht.message.Message;

/**
 * The {@link BencodeMessageCodec} encodes and decodes {@link Message}s
 * from Bencode.
 */
public class BencodeMessageCodec extends AbstractMessageCodec {
    
    private static final int DEFAULT_MESSAGE_SIZE = 1024;
    
    private final int messageSize;
    
    public BencodeMessageCodec() {
        this(DEFAULT_MESSAGE_SIZE);
    }
    
    public BencodeMessageCodec(int messageSize) {
        this.messageSize = NumberUtils.nextPowOfTwo(messageSize);
    }
    
    @Override
    public Message decode(SocketAddress src, byte[] data, int offset, int length)
            throws IOException {
        
        MessageInputStream in = null;
        try {
            in = new MessageInputStream(
                    new ByteArrayInputStream(data, offset, length));
            return in.readMessage(src);
        } finally {
            IoUtils.close(in);
        }
    }
    
    @Override
    public byte[] encode(Message message) throws IOException {
        MessageOutputStream out = null;
        try {
            ByteArrayOutputStream baos 
                = new ByteArrayOutputStream(messageSize);
            out = new MessageOutputStream(baos);
            out.writeMessage(message);
            return baos.toByteArray();
        } finally {
            IoUtils.close(out);
        }
    }
}