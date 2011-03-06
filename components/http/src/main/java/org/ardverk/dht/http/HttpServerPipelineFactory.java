package org.ardverk.dht.http;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

class HttpServerPipelineFactory implements ChannelPipelineFactory {

    private final HttpRequestHandler requestHandler;
    
    public HttpServerPipelineFactory(HttpRequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }
    
    @Override
    public ChannelPipeline getPipeline() {
        ChannelPipeline pipeline = Channels.pipeline();
        pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("encoder", new HttpResponseEncoder());
        pipeline.addLast("handler", requestHandler);
        return pipeline;
    }
}