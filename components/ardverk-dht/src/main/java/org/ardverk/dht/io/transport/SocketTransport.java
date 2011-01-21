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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

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
 * An (experimental) implementation of {@link Transport} that uses 
 * {@link Socket} and {@link ServerSocket}.
 */
public class SocketTransport extends AbstractTransport implements Closeable {

    private static final Logger LOG 
        = LoggerFactory.getLogger(DatagramTransport.class);
    
    private static final ExecutorService EXECUTOR 
        = ExecutorUtils.newCachedThreadPool("SocketTransportThread");
    
    private static final int CONNECT_TIMEOUT = 5000;
    
    private final ExecutorGroup executor 
        = new ExecutorGroup(EXECUTOR);
    
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
        this(new DefaultMessageCodec(), bindaddr);
    }
    
    public SocketTransport(MessageCodec codec, 
            SocketAddress bindaddr) {
        this.codec = Arguments.notNull(codec, "codec");
        this.bindaddr = Arguments.notNull(bindaddr, "bindaddr");
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
        ServerSocket socket = null;
        while ((socket = this.socket) != null 
                && !socket.isClosed()) {
            
            try {
                Socket client = socket.accept();
                process(client);
            } catch (IOException err) {
                uncaughtException(socket, err);
            }
        }
    }
    
    private void process(final Socket socket) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                SocketAddress src = socket.getRemoteSocketAddress();
                
                DataInputStream in = null;
                try {
                    in = new DataInputStream(
                            new BufferedInputStream(
                                    socket.getInputStream()));
                    socket.shutdownOutput();
                    
                    int length = in.readInt();
                    byte[] data = new byte[length];
                    in.readFully(data);
                    
                    Message message = codec.decode(src, data);
                    received(message);
                } catch (IOException err) {
                    uncaughtException(socket, err);
                } finally {
                    IoUtils.close(in);
                    IoUtils.close(socket);
                }
            }
        };
        
        executor.execute(task);
    }
    
    @Override
    public void send(final Message message, final ExceptionCallback callback)
                throws IOException {
        
        if (socket == null) {
            throw new IOException();
        }
        
        final SocketAddress dst = message.getAddress();
        final byte[] encoded = codec.encode(message);
        
        Runnable task = new Runnable() {
            @Override
            public void run() {
                Socket socket = null;
                DataOutputStream out = null;
                try {
                    socket = new Socket();
                    socket.connect(NetworkUtils.getResolved(dst), CONNECT_TIMEOUT);
                    
                    socket.shutdownInput();
                    out = new DataOutputStream(
                            new BufferedOutputStream(
                                socket.getOutputStream()));
                    
                    out.writeInt(encoded.length);
                    out.write(encoded);
                    
                } catch (IOException err) {
                    uncaughtException(socket, err);
                    
                    if (callback != null) {
                        callback.handleException(message, err);
                    }
                    
                } finally {
                    IoUtils.close(out);
                    IoUtils.close(socket);
                }
            }
        };
        
        executor.execute(task);
    }
    
    protected void uncaughtException(ServerSocket socket, Throwable t) {
        uncaughtException(socket.isClosed(), t);
    }
    
    protected void uncaughtException(Socket socket, Throwable t) {
        uncaughtException(socket.isClosed(), t);
    }
    
    private static void uncaughtException(boolean closed, Throwable t) {
        if (closed) {
            LOG.debug("Exception", t);
        } else {
            LOG.error("Exception", t);
        }
    }
}