package com.ardverk.dht.io.mina;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.DatagramSessionConfig;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;

import com.ardverk.dht.io.session.AbstractSessionContext;
import com.ardverk.dht.io.transport.AbstractTransport;
import com.ardverk.dht.io.transport.TransportListener;

public class MinaTransport extends AbstractTransport implements Closeable {

    private final NioDatagramAcceptor acceptor 
        = new NioDatagramAcceptor();
    
    public MinaTransport(SocketAddress address) throws IOException {
        
        DatagramSessionConfig config 
            = acceptor.getSessionConfig();
        config.setReuseAddress(true);
        
        IoHandler handler = new IoHandlerAdapter() {
            @Override
            public void messageReceived(IoSession session, Object message)
                    throws IOException {
                IoBuffer buffer = (IoBuffer)message;
                byte[] data = new byte[buffer.remaining()];
                buffer.get(data);
                received(session.getRemoteAddress(), data);
            }
        };
        
        acceptor.setHandler(handler);
        acceptor.bind(address);
    }
    
    @Override
    public void close() {
        if (acceptor != null) {
            acceptor.unbind();
            acceptor.dispose();
        }
    }
    
    @Override
    public void send(SocketAddress dst, byte[] message, int offset, int length)
            throws IOException {
        IoSession session = acceptor.newSession(
                dst, acceptor.getLocalAddress());
        session.write(IoBuffer.wrap(message, offset, length));
    }
    
    private static class SessionImpl extends AbstractSessionContext {
        
        private final IoSession session;
        
        private SessionImpl(IoSession session) {
            this.session = session;
        }

        @Override
        public InetSocketAddress getLocalAddress() {
            return (InetSocketAddress)session.getLocalAddress();
        }
        
        @Override
        public InetSocketAddress getRemoteAddress() {
            return (InetSocketAddress)session.getRemoteAddress();
        }
        
        @Override
        public String toString() {
            return session.toString();
        }
    }
    
    public static void main(String[] args) throws IOException {
        InetSocketAddress address = new InetSocketAddress(5555);
        MinaTransport transport = new MinaTransport(address);
        transport.addTransportListener(new TransportListener() {
            
            @Override
            public void received(SocketAddress src, byte[] message) throws IOException {
                System.out.println(src + ", " + message);
            }
        });
        
        byte[] data = "Hello World!".getBytes();

        transport.send(new InetSocketAddress("localhost", 5555), data);
        transport.send(new InetSocketAddress("localhost", 5555), data);
        transport.send(new InetSocketAddress("localhost", 5555), data);
        
        DatagramPacket packet = new DatagramPacket(data, data.length);
        packet.setSocketAddress(new InetSocketAddress("localhost", 5555));
        
        DatagramSocket socket = new DatagramSocket(1234);
        //socket.send(packet);
    }
}
