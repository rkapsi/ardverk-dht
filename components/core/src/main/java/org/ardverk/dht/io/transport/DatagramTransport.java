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

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.ExecutorGroup;
import org.ardverk.concurrent.ExecutorUtils;
import org.ardverk.dht.codec.DefaultMessageCodec;
import org.ardverk.dht.codec.MessageCodec;
import org.ardverk.dht.message.Message;
import org.ardverk.io.IoUtils;
import org.ardverk.lang.Arguments;
import org.ardverk.net.NetworkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@link Transport} that uses {@link DatagramSocket}s.
 */
public class DatagramTransport extends AbstractTransport implements Closeable {

    private static final Logger LOG 
        = LoggerFactory.getLogger(DatagramTransport.class);
    
    private static final ExecutorService EXECUTOR 
        = ExecutorUtils.newCachedThreadPool("DatagramTransportThread");
    
    private final int MAX_SIZE = 8 * 1024;
    
    private final ExecutorGroup executor 
        = new ExecutorGroup(EXECUTOR);
    
    private final MessageCodec codec;
    
    private final SocketAddress bindaddr;
    
    private volatile DatagramSocket socket = null;
    
    private Future<?> future = null;
    
    private boolean open = true;
    
    public DatagramTransport(MessageCodec codec, int port) {
        this(codec, new InetSocketAddress(port));
    }
    
    public DatagramTransport(MessageCodec codec, 
            String bindaddr, int port) {
        this(codec, new InetSocketAddress(bindaddr, port));
    }
    
    public DatagramTransport(MessageCodec codec, 
            InetAddress bindaddr, int port) {
        this(codec, new InetSocketAddress(bindaddr, port));
    }
    
    public DatagramTransport(SocketAddress bindaddr) {
        this(new DefaultMessageCodec(), bindaddr);
    }
    
    public DatagramTransport(MessageCodec codec, 
            SocketAddress bindaddr) {
        this.codec = Arguments.notNull(codec, "codec");
        this.bindaddr = Arguments.notNull(bindaddr, "bindaddr");
    }
    
    @Override
    public SocketAddress getSocketAddress() {
        return bindaddr;
    }

    @Override
    public synchronized void bind(TransportCallback.Inbound callback) throws IOException {
        if (!open) {
            throw new IOException();
        }
        
        super.bind(callback);

        socket = new DatagramSocket(bindaddr);

        Runnable task = new Runnable() {
            @Override
            public void run() {
                doServe();
            }
        };
        
        future = EXECUTOR.submit(task);
    }

    @Override
    public synchronized void unbind() {
        super.unbind();
        
        IoUtils.close(socket);
        
        if (future != null) {
            future.cancel(true);
        }
        
        synchronized (executor) {
            executor.getQueue().clear();
        }
    }

    @Override
    public synchronized void close() {
        open = false;
        executor.shutdownNow();
        unbind();
    }
    
    private void doServe() {
        
        byte[] buffer = new byte[MAX_SIZE];
        DatagramPacket packet 
            = new DatagramPacket(buffer, buffer.length);
        
        DatagramSocket socket = null;
        while ((socket = this.socket) != null 
                && !socket.isClosed()) {
            
            try {
                packet.setData(buffer);
                socket.receive(packet);
                
                process(packet);
            } catch (IOException err) {
                uncaughtException(socket, err);
            }
        }
    }
    
    private void process(DatagramPacket packet) {
        
        final SocketAddress src = packet.getSocketAddress();
        final byte[] data = extract(packet);
        
        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    Message message = codec.decode(src, data);
                    messageReceived(message);
                } catch (IOException err) {
                    uncaughtException(socket, err);
                }
            }
        };
        
        executor.execute(task);
    }
    
    @Override
    public void send(final Message message, final TransportCallback.Outbound callback,
            long timeout, TimeUnit unit) 
                throws IOException {
        
        final DatagramSocket socket = this.socket;
        if (socket == null) {
            throw new IOException();
        }
        
        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    
                    SocketAddress endpoint = NetworkUtils.getResolved(
                            message.getAddress());
                    byte[] encoded = codec.encode(message);
                    
                    DatagramPacket packet = new DatagramPacket(
                            encoded, 0, encoded.length, endpoint);
                    
                    socket.send(packet);
                    messageSent(callback, message);
                    
                } catch (IOException err) {
                    uncaughtException(socket, err);
                    handleException(callback, message, err);
                }
            }
        };
        
        executor.execute(task);
    }
    
    protected void uncaughtException(DatagramSocket socket, Throwable t) {
        if (socket.isClosed()) {
            LOG.debug("Exception", t);
        } else {
            LOG.error("Exception", t);
        }
    }
    
    /**
     * Extracts and returns a copy of the {@link DatagramPacket}'s {@code byte[]}.
     * 
     * @see DatagramPacket#getData()
     */
    private static byte[] extract(DatagramPacket packet) {
        byte[] data = packet.getData();
        int offset = packet.getOffset();
        int length = packet.getLength();
        
        byte[] copy = new byte[length];
        System.arraycopy(data, offset, copy, 0, copy.length);
        
        return copy;
    }
}