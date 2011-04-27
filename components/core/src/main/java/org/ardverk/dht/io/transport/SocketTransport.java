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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.ExecutorUtils;
import org.ardverk.dht.codec.MessageCodec;
import org.ardverk.dht.codec.MessageCodec.Decoder;
import org.ardverk.dht.codec.MessageCodec.Encoder;
import org.ardverk.dht.codec.bencode.BencodeMessageCodec;
import org.ardverk.dht.message.Message;
import org.ardverk.io.IoUtils;
import org.ardverk.net.NetworkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketTransport extends AbstractTransport implements Closeable {

    private static final Logger LOG 
        = LoggerFactory.getLogger(SocketTransport.class);
    
    private static final ExecutorService EXECUTOR 
        = ExecutorUtils.newCachedThreadPool("SocketTransportThread");
    
    private static final int DEFAULT_TIMEOUT = 10000;
    
    private final MessageCodec codec;
    
    private final SocketAddress bindaddr;
    
    private volatile ServerSocket socket = null;
    
    private Future<?> future = null;
    
    private boolean open = true;
    
    public SocketTransport(MessageCodec codec, int port) {
        this(codec, new InetSocketAddress(port));
    }
    
    public SocketTransport(MessageCodec codec, 
            String bindaddr, int port) {
        this(codec, new InetSocketAddress(bindaddr, port));
    }
    
    public SocketTransport(MessageCodec codec, 
            InetAddress bindaddr, int port) {
        this(codec, new InetSocketAddress(bindaddr, port));
    }
    
    public SocketTransport(SocketAddress bindaddr) {
        this(new BencodeMessageCodec(), bindaddr);
    }
    
    public SocketTransport(MessageCodec codec, 
            SocketAddress bindaddr) {
        this.codec = codec;
        this.bindaddr = bindaddr;
    }
    
    @Override
    public SocketAddress getSocketAddress() {
        return bindaddr;
    }
    
    @Override
    public synchronized void bind(TransportCallback callback) throws IOException {
        if (!open) {
            throw new IOException();
        }
        
        super.bind(callback);
    
        socket = new ServerSocket();
        //socket.setReuseAddress(true);
        //socket.setReceiveBufferSize(64*1024);
        //socket.bind(bindaddr, 512);
        socket.bind(bindaddr);
        
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
    }
    
    @Override
    public synchronized void close() {
        open = false;
        unbind();
    }
    
    private void doServe() {
        ServerSocket socket = null;
        Socket client = null;
        
        while ((socket = this.socket) != null 
                && !socket.isClosed()) {
            
            boolean processing = false;
            try {
                client = socket.accept();
                processing = process(client);
            } catch (IOException err) {
                uncaughtException(socket, err);
            } finally {
                if (!processing) {
                    IoUtils.close(client);
                }
            }
        }
    }
    
    private boolean process(final Socket client) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                SocketAddress src = client.getRemoteSocketAddress();
                
                Decoder decoder = null;
                boolean success = false;
                try {
                    decoder = codec.createDecoder(src, 
                            new BufferedInputStream(
                                client.getInputStream()));
                    
                    Message message = decoder.read();
                    
                    messageReceived(new Endpoint() {
                        @Override
                        public void send(Message message, long timeout,
                                TimeUnit unit) throws IOException {
                            Encoder encoder = null;
                            try {
                                encoder = codec.createEncoder(
                                        new BufferedOutputStream(
                                            client.getOutputStream()));
                            
                                encoder.write(message);
                                encoder.flush();
                                
                                messageSent(message);
                            } finally {
                                close(client, encoder);
                            }
                        }
                    }, message);
                    
                    success = true;
                    
                } catch (IOException err) {
                    uncaughtException(client, err);
                } finally {
                    IoUtils.close(decoder);
                    
                    if (!success) {
                        IoUtils.close(client);
                    }
                }
            }
        };
        
        EXECUTOR.execute(task);
        return true;
    }
    
    @Override
    public void send(final Message message, final long timeout, 
            final TimeUnit unit) throws IOException {
        
        ServerSocket socket = this.socket;
        if (socket == null || socket.isClosed()) {
            throw new IOException();
        }
        
        Runnable task = new Runnable() {
            @Override
            public void run() {
                Socket client = null;
                Encoder encoder = null;
                Decoder decoder = null;
                try {
                    client = new Socket();
                    //client.setReuseAddress(true);
                    //client.setSendBufferSize(64*1024);
                    
                    int timeoutInMillis = (int)unit.toMillis(timeout);
                    if (timeoutInMillis < 0) {
                        timeoutInMillis = DEFAULT_TIMEOUT;
                    }
                    
                    SocketAddress dst = message.getAddress();
                    
                    SocketAddress endpoint 
                        = NetworkUtils.getResolved(dst);
                    
                    client.connect(endpoint, timeoutInMillis);
                    
                    encoder = codec.createEncoder(
                                new BufferedOutputStream(
                                    client.getOutputStream()));
                    
                    encoder.write(message);
                    encoder.flush();
                    
                    messageSent(message);
                    
                    SocketAddress src = client.getRemoteSocketAddress();
                    decoder = codec.createDecoder(src, 
                            new BufferedInputStream(
                                client.getInputStream()));
                    
                    Message message = decoder.read();
                    messageReceived(message);
                    
                } catch (IOException err) {
                    uncaughtException(client, err);
                    handleException(message, err);
                    
                } finally {
                    close(client, encoder, decoder);
                }
            }
        };
        
        EXECUTOR.execute(task);
    }
    
    private static void uncaughtException(ServerSocket socket, Throwable t) {
        uncaughtException(socket.isClosed(), t);
    }
    
    private static void uncaughtException(Socket socket, Throwable t) {
        uncaughtException(socket.isClosed(), t);
    }
    
    private static void uncaughtException(boolean closed, Throwable t) {
        if (closed) {
            LOG.debug("Exception", t);
        } else {
            LOG.error("Exception", t);
        }
    }
    
    private static void close(Socket client, Closeable... closeable) {
        IoUtils.closeAll(closeable);
        IoUtils.close(client);
    }
}