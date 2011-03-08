package org.ardverk.dht.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.ardverk.dht.codec.MessageCodec;
import org.ardverk.dht.codec.bencode.BencodeMessageCodec;
import org.ardverk.dht.io.transport.AbstractTransport;
import org.ardverk.dht.io.transport.Endpoint;
import org.ardverk.dht.io.transport.TransportCallback;
import org.ardverk.dht.message.Message;
import org.ardverk.dht.message.RequestMessage;
import org.ardverk.dht.message.ResponseMessage;
import org.ardverk.net.NetworkUtils;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpClientCodec;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

public class HttpTransport extends AbstractTransport {
    
    private static final Executor EXECUTOR = Executors.newCachedThreadPool();
    
    static {
        ((ThreadPoolExecutor)EXECUTOR).setKeepAliveTime(10L, TimeUnit.SECONDS);
    }
    
    private final MessageCodec codec = new BencodeMessageCodec();
    
    private final HttpRequestHandler requestHandler 
        = new DefaultHttpRequestHandler();
    
    private final SocketAddress bindaddr;
    
    private final ServerBootstrap server;
    
    private final ClientSocketChannelFactory channelFactory;
    
    private Channel acceptor;
    
    public HttpTransport(int port) {
        this(new InetSocketAddress(port));
    }
    
    public HttpTransport(String host, int port) {
        this(new InetSocketAddress(host, port));
    }
    
    public HttpTransport(InetAddress address, int port) {
        this(new InetSocketAddress(address, port));
    }
    
    public HttpTransport(SocketAddress bindaddr) {
        this.bindaddr = bindaddr;
        
        server = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                    EXECUTOR, EXECUTOR));
        server.setPipelineFactory(
                new HttpServerPipelineFactory(requestHandler));
        
        channelFactory = new NioClientSocketChannelFactory(
                EXECUTOR, EXECUTOR);
    }
    
    @Override
    public SocketAddress getSocketAddress() {
        return bindaddr;
    }

    @Override
    public void bind(TransportCallback callback) throws IOException {
        super.bind(callback);
        acceptor = server.bind(bindaddr);
    }

    @Override
    public void unbind() {
        if (acceptor != null) {
            acceptor.close();
        }
        
        super.unbind();
    }

    private ChannelFuture connect(SocketAddress dst) {
        ChannelPipeline pipeline = Channels.pipeline();
        pipeline.addLast("codec", new HttpClientCodec());
        pipeline.addLast("handler", new DefaultHttpResponseHandler(dst));
        
        ClientBootstrap client = new ClientBootstrap(channelFactory);
        client.setPipeline(pipeline);
        return client.connect(dst);
    }
    
    @Override
    public void send(final Message message, long timeout, 
            TimeUnit unit) throws IOException {
        
        assert (message instanceof RequestMessage);
        
        SocketAddress dst = NetworkUtils.getResolved(
                message.getAddress());
        System.out.println("SENDING: " + dst);
        ChannelFuture future = connect(dst);
        
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(final ChannelFuture connectFuture) throws IOException {
                if (!connectFuture.isSuccess()) {
                    handleException(message, connectFuture.getCause());
                    return;
                }
                
                byte[] data = codec.encode(message);
                
                HttpRequest request = new DefaultHttpRequest(
                        HttpVersion.HTTP_1_1, 
                        HttpMethod.POST, "/ardverk");
                request.setContent(ChannelBuffers.copiedBuffer(data));
                request.setHeader(HttpHeaders.Names.CONTENT_LENGTH, data.length);
                request.setHeader(HttpHeaders.Names.CONNECTION, 
                        HttpHeaders.Values.CLOSE);
                
                Channel channel = connectFuture.getChannel();
                ChannelFuture future = channel.write(request);
                
                future.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                future.addListener(new MessageListener(HttpTransport.this, message));
            }
        });
    }
    
    private class DefaultHttpRequestHandler extends HttpRequestHandler {
        
        @Override
        public void messageReceived(ChannelHandlerContext ctx, final MessageEvent e)
                throws Exception {
            HttpRequest request = (HttpRequest)e.getMessage();
            
            SocketAddress src = e.getRemoteAddress();
            
            System.out.println("RECEIVED #1: " + src);
            
            ChannelBuffer content = request.getContent();
            
            Message message = codec.decode(src, content.array());
            assert (message instanceof RequestMessage);
            
            System.out.println("RECEIVED #1: " + message.getContact());
            
            HttpTransport.this.messageReceived(new Endpoint() {
                @Override
                public void send(final Message message, long timeout,
                        TimeUnit unit) throws IOException {
                    
                    assert (message instanceof ResponseMessage);
                    
                    byte[] data = codec.encode(message);
                    
                    HttpResponse response = new DefaultHttpResponse(
                            HttpVersion.HTTP_1_1, 
                            HttpResponseStatus.OK);
                    response.setContent(ChannelBuffers.copiedBuffer(data));
                    response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, data.length);
                    response.setHeader(HttpHeaders.Names.CONNECTION, 
                            HttpHeaders.Values.CLOSE);
                    
                    Channel channel = e.getChannel();
                    ChannelFuture future = channel.write(response);
                    future.addListener(ChannelFutureListener.CLOSE);
                    future.addListener(new MessageListener(this, message));
                    
                }
            }, message);
        }
    }
    
    private class DefaultHttpResponseHandler extends HttpResponseHandler {
        
        private final SocketAddress dst;
        
        public DefaultHttpResponseHandler(SocketAddress dst) {
            this.dst = dst;
        }
        
        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
                throws Exception {
            HttpResponse response = (HttpResponse)e.getMessage();
            
            SocketAddress src = e.getRemoteAddress();
            
            System.out.println("RECEIVED #2: " + src + " vs. " + e.getChannel().getLocalAddress());
            
            ChannelBuffer content = response.getContent();
            
            Message message = codec.decode(dst, content.array());
            
            HttpTransport.this.messageReceived(message);
        }
    }
    
    private class MessageListener implements ChannelFutureListener {
        
        private final Endpoint endpoint;
        
        private final Message message;
        
        public MessageListener(Endpoint endpoint, Message message) {
            this.message = message;
            this.endpoint = endpoint;
        }

        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            if (future.isSuccess()) {
                HttpTransport.this.messageSent(endpoint, message);
            } else {
                HttpTransport.this.handleException(endpoint, message, future.getCause());
            }
        }
    }
}
