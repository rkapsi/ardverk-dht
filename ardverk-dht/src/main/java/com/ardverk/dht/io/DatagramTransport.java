package com.ardverk.dht.io;

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
import org.ardverk.lang.NullArgumentException;
import org.slf4j.Logger;

import com.ardverk.dht.io.transport.AbstractTransport;
import com.ardverk.dht.io.transport.TransportCallback;
import com.ardverk.logging.LoggerUtils;

public class DatagramTransport extends AbstractTransport implements Closeable {

    private static final Logger LOG 
        = LoggerUtils.getLogger(DatagramTransport.class);
    
    private static final ExecutorService EXECUTOR 
        = ExecutorUtils.newCachedThreadPool("DatagramTransportThread");
    
    private final ExecutorGroup executor 
        = new ExecutorGroup(EXECUTOR);
    
    private final SocketAddress bindaddr;
    
    private volatile DatagramSocket socket = null;
    
    private Future<?> future = null;
    
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
        if (bindaddr == null) {
            throw new NullArgumentException("bindaddr");
        }
        
        this.bindaddr = bindaddr;
    }
    
    @Override
    public synchronized void bind(TransportCallback callback) throws IOException {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                doServe();
            }
        };
        
        socket = new DatagramSocket(bindaddr);
        future = EXECUTOR.submit(task);
        super.bind(callback);
    }

    @Override
    public synchronized void unbind() {
        if (socket != null) {
            socket.close();
        }
        
        if (future != null) {
            future.cancel(true);
        }
        
        synchronized (executor) {
            executor.getQueue().clear();
        }
        
        super.unbind();
    }

    @Override
    public synchronized void close() {
        executor.shutdownNow();
        unbind();
    }
    
    private void doServe() {
        DatagramSocket socket = null;
        while ((socket = this.socket) != null 
                && !socket.isClosed()) {
            
            try {
                DatagramPacket packet 
                    = receive(socket);
                process(packet);
            } catch (IOException err) {
                uncaughtException(err);
            }
        }
    }
    
    private static DatagramPacket receive(DatagramSocket socket) throws IOException {
        byte[] message = new byte[8 * 1024];
        DatagramPacket packet = new DatagramPacket(
                message, 0, message.length);
        socket.receive(packet);
        return packet;
    }
    
    private void process(final DatagramPacket packet) throws IOException {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                SocketAddress src = packet.getSocketAddress();
                byte[] message = packet.getData();
                int offset = packet.getOffset();
                int length = packet.getLength();
                
                try {
                    received(src, message, offset, length);
                } catch (IOException err) {
                    uncaughtException(err);
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
            
        }
        
        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    socket.send(packet);
                } catch (IOException err) {
                    uncaughtException(err);
                }
            }
        };
        
        executor.execute(task);
    }
    
    protected void uncaughtException(Throwable t) {
        LOG.error("Exception", t);
    }
}
