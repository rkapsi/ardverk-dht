package com.ardverk.dht.io;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;

import com.ardverk.dht.io.transport.Transport;
import com.ardverk.dht.io.transport.TransportListener;
import com.ardverk.dht.message.Message;
import com.ardverk.dht.message.MessageCodec;
import com.ardverk.dht.message.MessageId;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.logging.LoggerUtils;

public abstract class MessageDispatcher implements Closeable {
    
    private static final Logger LOG 
        = LoggerUtils.getLogger(MessageDispatcher.class);
    
    private final TransportListener listener 
            = new TransportListener() {
        @Override
        public void received(SocketAddress src, 
                byte[] message) throws IOException {
            MessageDispatcher.this.received(src, message);
        }
    };
    
    private final MessageEntityManager entityManager 
        = new MessageEntityManager();
    
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
        entityManager.close();
    }
    
    public void send(ResponseMessage message) throws IOException {
        byte[] data = codec.encode(context, message);
        transport.send(message.getAddress(), data);
    }
    
    public void send(MessageCallback callback, 
            RequestMessage message, long timeout, 
            TimeUnit unit) throws IOException {
        
        byte[] data = codec.encode(context, message);
        
        entityManager.add(callback, message, timeout, unit);
        transport.send(message.getAddress(), data);
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
    
    private void response(SocketAddress src, ResponseMessage message) throws Exception {
        MessageEntity entity = entityManager.get(message);
        
        if (entity != null) {
            entity.handleResponse(message);
        } else {
            lateResponse(message);
        }
    }
    
    protected abstract void lateResponse(ResponseMessage message);
    
    private static class MessageEntityManager implements Closeable {
        
        private static final ScheduledExecutorService EXECUTOR 
            = Executors.newSingleThreadScheduledExecutor();
        
        private final Map<MessageId, MessageEntity> callbacks 
            = Collections.synchronizedMap(new HashMap<MessageId, MessageEntity>());
        
        private boolean open = true;
        
        public void close() {
            synchronized (callbacks) {
                if (!open) {
                    return;
                }
                
                open = false;
                
                for (MessageEntity entity : callbacks.values()) {
                    entity.close();
                }
                
                callbacks.clear();
            }
        }
        
        public void add(MessageCallback callback, RequestMessage message, 
                long timeout, TimeUnit unit) {
            
            final MessageId messageId = message.getMessageId();
            
            synchronized (callbacks) {
                
                if (!open) {
                    throw new IllegalStateException();
                }
                
                Runnable task = new Runnable() {
                    @Override
                    public void run() {
                        MessageEntity entity 
                            = callbacks.remove(messageId);
                        
                        if (entity != null) {
                            entity.handleTimeout();
                        }
                    }
                };
                
                ScheduledFuture<?> future 
                    = EXECUTOR.schedule(task, timeout, unit);
                
                MessageEntity entity = new MessageEntity(
                        future, callback, message);
                callbacks.put(messageId, entity);
            }
        }
        
        public MessageEntity get(ResponseMessage message) {
            synchronized (callbacks) {
                MessageEntity entity = callbacks.remove(
                        message.getMessageId());
                if (entity != null) {
                    entity.close();
                }
                return entity;
            }
        }
    }

    /**
     * 
     */
    private static class MessageEntity implements Closeable {
        
        private final ScheduledFuture<?> future;

        private final MessageCallback callback;
        
        private final RequestMessage request;
        
        private final AtomicBoolean done = new AtomicBoolean();
        
        private MessageEntity(ScheduledFuture<?> future, 
                MessageCallback callback, 
                RequestMessage request) {
            
            if (future == null) {
                throw new NullPointerException("future");
            }
            
            if (callback == null) {
                throw new NullPointerException("callback");
            }
            
            if (request == null) {
                throw new NullPointerException("request");
            }
            
            this.future = future;
            this.callback = callback;
            this.request = request;
        }

        @Override
        public void close() {
            future.cancel(true);
        }
        
        public void handleResponse(ResponseMessage response) {
            close();
            
            try {
                if (!done.getAndSet(true)) {
                    callback.handleResponse(response);
                }
            } catch (Exception err) {
                LOG.error("Exception", err);
            }
        }

        public void handleTimeout() {
            close();
            
            try {
                if (!done.getAndSet(true)) {
                    callback.handleTimeout(request);
                }
            } catch (Exception err) {
                LOG.error("Exception", err);
            }
        }
    }
}
