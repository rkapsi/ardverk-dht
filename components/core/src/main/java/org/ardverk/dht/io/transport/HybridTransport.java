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

package org.ardverk.dht.io.transport;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import org.ardverk.dht.codec.MessageCodec;
import org.ardverk.dht.message.Message;
import org.ardverk.dht.message.StoreRequest;
import org.ardverk.dht.message.StoreResponse;
import org.ardverk.dht.message.ValueResponse;
import org.ardverk.io.IoUtils;

/**
 * An (experimental) implementation of {@link Transport} that uses UDP 
 * for most operations and TCP for some operations.
 * 
 * <ul>
 * <li>TCP: STORE (request+response) and FIND_VALUE (response)
 * <li>UDP: Everything else
 * </ul>
 * 
 * @see DatagramTransport
 * @see SocketTransport
 */
public class HybridTransport extends DatagramTransport {

    private final SocketTransport socket;
    
    public HybridTransport(MessageCodec codec, int port) {
        this(codec, new InetSocketAddress(port));
    }

    public HybridTransport(MessageCodec codec, SocketAddress bindaddr) {
        super(codec, bindaddr);
        socket = new SocketTransport(codec, bindaddr);
    }

    @Override
    public void close() {
        super.close();
        IoUtils.close(socket);
    }
    
    @Override
    public void bind(TransportCallback callback) throws IOException {
        super.bind(callback);
        socket.bind(callback);
    }

    @Override
    public void unbind() {
        super.unbind();
        socket.unbind();
    }
    
    /**
     * Returns {@code true} if the given {@link Message} 
     * should be send over TCP.
     */
    protected boolean isUseTCP(Message message) {
        return message instanceof StoreRequest
            || message instanceof StoreResponse
            || message instanceof ValueResponse;
    }
    
    @Override
    public void send(Message message, long timeout, 
            TimeUnit unit) throws IOException {
        
        if (isUseTCP(message)) {
            socket.send(message, timeout, unit);
        } else {
            super.send(message, timeout, unit);
        }
    }
}