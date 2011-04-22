/*
 * Copyright 2009-2011 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ardverk.dht.codec.bencode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketAddress;

import org.ardverk.dht.codec.MessageCodec;
import org.ardverk.dht.message.Message;


/**
 * The {@link BencodeMessageCodec} encodes and decodes {@link Message}s
 * from Bencode.
 */
public class BencodeMessageCodec implements MessageCodec {

    @Override
    public Decoder createDecoder(final SocketAddress src, final InputStream in) {
        Decoder decoder = new Decoder() {
            
            private final MessageInputStream mis = new MessageInputStream(in);
            
            @Override
            public Message read() throws IOException {
                return mis.readMessage(src);
            }
            
            @Override
            public void close() throws IOException {
                mis.close();
            }
        };
        return decoder;
    }

    @Override
    public Encoder createEncoder(final OutputStream out) {
        Encoder encoder = new Encoder() {
            
            private final MessageOutputStream mos = new MessageOutputStream(out);
            
            @Override
            public void write(Message message) throws IOException {
                mos.writeMessage(message);
            }
            
            @Override
            public void flush() throws IOException {
                mos.flush();
            }
            
            @Override
            public void close() throws IOException {
                mos.close();
            }
        };
        return encoder;
    }
}