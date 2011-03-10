package org.ardverk.dht.http;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.codec.http.HttpClientCodec;

class HttpClientPipelineFactory implements ChannelPipelineFactory {

    private final SimpleChannelHandler channelHandler;
    
    public HttpClientPipelineFactory(SimpleChannelHandler channelHandler) {
        this.channelHandler = channelHandler;
    }
    
    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = Channels.pipeline();
        pipeline.addLast("codec", new HttpClientCodec());
        pipeline.addLast("idle", IdleUtils.DEFAULT);
        pipeline.addLast("handler", channelHandler);
        return pipeline;
    }
}
