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

package org.ardverk.dht.codec;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketAddress;

import org.ardverk.dht.message.Message;
import org.ardverk.dht.storage.ResourceFactory;


/**
 * A {@link MessageCodec} encodes and decodes {@link Message}s.
 */
public interface MessageCodec {
    
    /**
     * Returns the {@link ResourceFactory}.
     */
    public ResourceFactory getResourceFactory();
    
    /**
     * Creates and returns a {@link Decoder}.
     */
    public Decoder createDecoder(SocketAddress src, InputStream in) throws IOException;
    
    /**
     * Creates and retruns an {@link Encoder}.
     */
    public Encoder createEncoder(OutputStream out) throws IOException;
    
    /**
     * A {@link Decoder} decodes {@link Message}s.
     */
    public static interface Decoder extends Closeable {
        /**
         * Reads and returns a {@link Message}.
         */
        public Message read() throws IOException;
    }
    
    /**
     * A {@link Encoder} encodes {@link Message}s.
     */
    public static interface Encoder extends Flushable, Closeable {
        /**
         * Writes the given {@link Message}.
         */
        public void write(Message message) throws IOException;
    }
}