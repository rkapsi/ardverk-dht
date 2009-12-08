package com.ardverk.dht.io;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.ardverk.dht.io.transport.Transport;
import com.ardverk.dht.io.transport.TransportListener;
import com.ardverk.dht.message.Message;
import com.ardverk.dht.message.MessageCodec;
import com.ardverk.dht.message.MessageId;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;

public abstract class MessageDispatcher implements Closeable {

    private final TransportListener listener 
            = new TransportListener() {
        @Override
        public void received(SocketAddress src, 
                byte[] message) throws IOException {
            MessageDispatcher.this.received(src, message);
        }
    };
    
    private final Map<MessageId, MessageHandler<? extends Message>> callbacks 
        = new ConcurrentHashMap<MessageId, MessageHandler<? extends Message>>();
    
    private final Transport transport;
    
    private final MessageCodec codec;
    
    public MessageDispatcher(Transport transport, MessageCodec codec) {
        if (transport == null) {
            throw new NullPointerException("transport");
        }
        
        if (codec == null) {
            throw new NullPointerException("codec");
        }
        
        this.transport = transport;
        this.codec = codec;
        
        transport.addTransportListener(listener);
    }
    
    @Override
    public void close() {
        transport.removeTransportListener(listener);
    }
    
    public void send(MessageHandler<? extends Message> callback, 
            SocketAddress dst, Message message, long timeout, TimeUnit unit) throws IOException {
        byte[] data = codec.encode(context, message);
        transport.send(dst, data);
    }
    
    private void received(SocketAddress src, byte[] data) {
        Message message = codec.decode(context, data);
        if (message instanceof RequestMessage) {
            request(src, (RequestMessage)message);
        } else {
            response(src, (ResponseMessage)message);
        }
    }
    
    protected abstract void request(SocketAddress src, RequestMessage message);
    
    private void response(SocketAddress src, ResponseMessage message) {
        MessageId messageId = message.getMessageId();
        MessageHandler<? extends Message> callback 
            = callbacks.remove(messageId);
        
        if (callback != null) {
            response(callback, src, message);
        } else {
            lateResponse(src, message);
        }
    }
    
    private void response(MessageHandler<? extends Message> callback, 
            SocketAddress src, Message ResponseMessage) {
        
    }
    
    protected abstract void lateResponse(
            SocketAddress src, ResponseMessage message);
}
