package org.ardverk.dht.http;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

public class HttpRequestHandler extends SimpleChannelUpstreamHandler {
    
    private static final HttpResponse METHOD_NOT_ALLOWED = new DefaultHttpResponse(
            HttpVersion.HTTP_1_1, HttpResponseStatus.METHOD_NOT_ALLOWED);
    
    private static final HttpResponse NOT_IMPLEMENTED = new DefaultHttpResponse(
            HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_IMPLEMENTED);
    
    static {
        HttpHeaders.setKeepAlive(METHOD_NOT_ALLOWED, false);
        HttpHeaders.setKeepAlive(NOT_IMPLEMENTED, false);
    }
    
    @Override
    public void messageReceived(ChannelHandlerContext ctx, 
            MessageEvent e) throws Exception {
        HttpRequest request = (HttpRequest)e.getMessage();
        
        HttpResponse response = null;
        
        HttpMethod method = request.getMethod();
        if (method.equals(HttpMethod.GET)) {
            response = doGet(ctx, e);
        } else if (method.equals(HttpMethod.POST)) {
            response = doPost(ctx, e);
        } else {
            response = METHOD_NOT_ALLOWED;
        }
        
        if (response == null) {
            response = NOT_IMPLEMENTED;
        }
        
        Channel channel = e.getChannel();
        ChannelFuture future = channel.write(response);
        
        if (!HttpHeaders.isKeepAlive(request)
                || !HttpHeaders.isKeepAlive(response)) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }
    
    protected HttpResponse doGet(ChannelHandlerContext ctx, 
            MessageEvent e) throws Exception {
        return null;
    }
    
    protected HttpResponse doPost(ChannelHandlerContext ctx, 
            MessageEvent e) throws Exception {
        return null;
    }
}