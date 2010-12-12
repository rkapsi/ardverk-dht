/*
 * Copyright 2010 Roger Kapsi
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

package com.ardverk.dht.io.transport;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.ardverk.concurrent.ExecutorGroup;
import org.ardverk.concurrent.ExecutorUtils;
import org.ardverk.lang.Arguments;
import org.slf4j.Logger;

import com.ardverk.dht.logging.LoggerUtils;

public class DatagramTransport extends AbstractTransport implements Closeable {

    private static final Logger LOG 
        = LoggerUtils.getLogger(DatagramTransport.class);
    
    private static final ExecutorService EXECUTOR 
        = ExecutorUtils.newCachedThreadPool("DatagramTransportThread");
    
    private final int MAX_SIZE = 8 * 1024;
    
    private final ExecutorGroup executor 
        = new ExecutorGroup(EXECUTOR);
    
    private final SocketAddress bindaddr;
    
    private volatile DatagramSocket socket = null;
    
    private Future<?> future = null;
    
    private boolean open = true;
    
    public DatagramTransport(int port) {
        this(new InetSocketAddress(port));
    }
    
    public DatagramTransport(String bindaddr, int port) {
        this(new InetSocketAddress(bindaddr, port));
    }
    
    public DatagramTransport(InetAddress bindaddr, int port) {
        this(new InetSocketAddress(bindaddr, port));
    }
    
    public DatagramTransport(SocketAddress bindaddr) {
        this.bindaddr = Arguments.notNull(bindaddr, "bindaddr");
    }
    
    @Override
    public synchronized void bind(TransportCallback callback) throws IOException {
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
        
        if (socket != null) {
            socket.close();
        }
        
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
        
        byte[] data = packet.getData();
        int offset = packet.getOffset();
        int length = packet.getLength();
        
        final byte[] message = new byte[length];
        System.arraycopy(data, offset, message, 0, message.length);
        
        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    received(src, message, 0, message.length);
                } catch (IOException err) {
                    uncaughtException(socket, err);
                }
            }
        };
        
        executor.execute(task);
    }
    
    @Override
    public void send(SocketAddress dst, byte[] message, int offset, int length)
            throws IOException {
        
        final DatagramPacket packet = new DatagramPacket(
                message, offset, length, dst);
        
        final DatagramSocket socket = this.socket;
        
        if (socket == null) {
            throw new IOException();
        }
        
        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    socket.send(packet);
                } catch (IOException err) {
                    uncaughtException(socket, err);
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
}