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
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpTransport extends AbstractTransport {
    
    private static final Logger LOG 
        = LoggerFactory.getLogger(HttpTransport.class);
    
    private static final Executor EXECUTOR = Executors.newCachedThreadPool();
    
    static {
        ((ThreadPoolExecutor)EXECUTOR).setKeepAliveTime(10L, TimeUnit.SECONDS);
    }
    
    private final MessageCodec codec = new BencodeMessageCodec();
    
    private final SimpleChannelHandler requestHandler 
        = new DefaultHttpRequestHandler();
    
    private final SimpleChannelHandler responseHandler 
        = new DefaultHttpResponseHandler();
    
    private final HttpClientPipelineFactory pipelineFactory 
        = new HttpClientPipelineFactory(responseHandler);
    
    private final SocketAddress bindaddr;
    
    private final ServerBootstrap server;
    
    //private final ClientBootstrap client;
    
    private final NioClientSocketChannelFactory channelFactory;
    
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
        
        /*client = new ClientBootstrap(
                new NioClientSocketChannelFactory(
                    EXECUTOR, EXECUTOR));
        client.setPipelineFactory(
                new HttpClientPipelineFactory(responseHandler));*/
        
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
    
    private ChannelFuture connect(SocketAddress addr, long timeout, TimeUnit unit) {
        ClientBootstrap client 
            = new ClientBootstrap(channelFactory);
        client.setOption("connectTimeoutMillis", 
                Integer.valueOf((int)unit.toMillis(timeout)));
        client.setPipelineFactory(pipelineFactory);
        return client.connect(addr);
    }
    
    @Override
    public void send(final Message message, long timeout, 
            TimeUnit unit) throws IOException {
        
        assert (message instanceof RequestMessage);
        
        SocketAddress dst = NetworkUtils.getResolved(
                message.getAddress());
        
        ChannelFuture future = null;
        try {
            future = connect(dst, timeout, unit);
        } catch (Exception err) {
            LOG.error("Exception", err);
            handleException(message, err);
            return;
        }
        
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture connectFuture) throws IOException {
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
    
    private static class HttpHandler extends IdleStateAwareChannelHandler {

        @Override
        public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) {
            e.getChannel().close();
        }
    }
    
    private class DefaultHttpRequestHandler extends HttpHandler {
        
        @Override
        public void messageReceived(ChannelHandlerContext ctx, final MessageEvent e)
                throws Exception {
            HttpRequest request = (HttpRequest)e.getMessage();
            
            SocketAddress src = e.getRemoteAddress();
            ChannelBuffer content = request.getContent();
            
            Message message = codec.decode(src, content.array());
            assert (message instanceof RequestMessage);
            
            HttpTransport.this.messageReceived(new Endpoint() {
                @Override
                public void send(Message message, long timeout,
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
    
    private class DefaultHttpResponseHandler extends HttpHandler {
        
        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
                throws Exception {
            HttpResponse response = (HttpResponse)e.getMessage();
            
            SocketAddress src = e.getRemoteAddress();
            
            ChannelBuffer content = response.getContent();
            Message message = codec.decode(src, content.array());
            
            HttpTransport.this.messageReceived(message);
            
            e.getChannel().close();
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
