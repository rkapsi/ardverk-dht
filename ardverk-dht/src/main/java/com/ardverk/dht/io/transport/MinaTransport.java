package com.ardverk.dht.io.transport;

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

public class MinaTransport extends AbstractTransport {

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
                SocketAddress src = session.getRemoteAddress();
                IoBuffer data = (IoBuffer)message;
                MinaTransport.this.received(src, data.array(), 
                        data.arrayOffset(), data.remaining());
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
    
    public static void main(String[] args) throws IOException {
        InetSocketAddress address = new InetSocketAddress(5555);
        MinaTransport transport = new MinaTransport(address);
        transport.addTransportListener(new TransportListener() {
            
            @Override
            public void received(SocketAddress src, byte[] message, int offet,
                    int length) throws IOException {
                System.out.println(src + ", " + new String(message));
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
