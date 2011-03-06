package org.ardverk.dht.http;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpClientCodec;

class HttpClientPipelineFactory implements ChannelPipelineFactory {

    private final HttpResponseHandler responseHandler;
    
    public HttpClientPipelineFactory(HttpResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
    }
    
    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = Channels.pipeline();
        pipeline.addLast("codec", new HttpClientCodec());
        pipeline.addLast("handler", responseHandler);
        return pipeline;
    }
}
