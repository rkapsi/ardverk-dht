package com.ardverk.dht.io;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;

import com.ardverk.dht.io.transport.Transport;
import com.ardverk.dht.io.transport.TransportListener;
import com.ardverk.dht.message.Message;
import com.ardverk.dht.message.MessageCodec;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.message.MessageId;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.logging.LoggerUtils;
import com.ardverk.utils.ExecutorUtils;

/**
 * 
 */
public abstract class MessageDispatcher implements Closeable {
    
    private static final Logger LOG 
        = LoggerUtils.getLogger(MessageDispatcher.class);
    
    private final TransportListener listener 
            = new TransportListener() {
        @Override
        public void received(SocketAddress src, 
                byte[] message) throws IOException {
            MessageDispatcher.this.handleMessage(src, message);
        }
    };
    
    private final MessageEntityManager entityManager 
        = new MessageEntityManager();
    
    private final Transport transport;
    
    private final MessageFactory factory;
    
    private final MessageCodec codec;
    
    /**
     * 
     */
    public MessageDispatcher(Transport transport, 
            MessageFactory factory, MessageCodec codec) {
        
        if (transport == null) {
            throw new NullPointerException("transport");
        }
        
        if (factory == null) {
            throw new NullPointerException("factory");
        }
        
        if (codec == null) {
            throw new NullPointerException("codec");
        }
        
        this.transport = transport;
        this.factory = factory;
        this.codec = codec;
        
        transport.addTransportListener(listener);
    }
    
    /**
     * 
     */
    public Transport getTransport() {
        return transport;
    }
    
    /**
     * 
     */
    public MessageFactory getMessageFactory() {
        return factory;
    }
    
    /**
     * 
     */
    public MessageCodec getMessageCodec() {
        return codec;
    }
    
    @Override
    public void close() {
        transport.removeTransportListener(listener);
        entityManager.close();
    }
    
    /**
     * 
     */
    public void send(ResponseMessage message) throws IOException {
        byte[] data = codec.encode(message);
        transport.send(message.getAddress(), data);
    }
    
    /**
     * 
     */
    public void send(MessageCallback callback, 
            RequestMessage message, long timeout, 
            TimeUnit unit) throws IOException {
        
        byte[] data = codec.encode(message);
        
        entityManager.add(callback, message, timeout, unit);
        transport.send(message.getAddress(), data);
    }
    
    /**
     * 
     */
    private void handleMessage(SocketAddress src, 
            byte[] data) throws IOException {
        Message message = codec.decode(data);
        handleMessage(src, message);
    }
    
    /**
     * 
     */
    protected void handleMessage(SocketAddress src, 
            Message message) throws IOException {
        
        if (message instanceof RequestMessage) {
            handleRequest(src, (RequestMessage)message);
        } else {
            handleResponse(src, (ResponseMessage)message);
        }
    }
    
    /**
     * 
     */
    protected abstract void handleRequest(SocketAddress src, 
            RequestMessage message) throws IOException;
    
    /**
     * 
     */
    protected void handleResponse(SocketAddress src, 
            ResponseMessage response) throws IOException {
        
        MessageId messageId = response.getMessageId();
        if (!factory.isFor(messageId, src)) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Wrong MessageId signature: " 
                        + src + ", " + messageId);
            }
            return;
        }
        
        MessageEntity entity = entityManager.get(response);
        boolean success = false;
        if (entity != null) {
            success = entity.handleResponse(response);
        }
        
        if (!success) {
            lateResponse(response);
        }
    }
    
    /**
     * 
     */
    protected abstract void lateResponse(
            ResponseMessage message) throws IOException;
    
    /**
     * 
     */
    private static class MessageEntityManager implements Closeable {
        
        private static final ScheduledExecutorService EXECUTOR 
            = ExecutorUtils.newSingleThreadScheduledExecutor(
                    "MessageDispatcherThread");
        
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
        
        /**
         * 
         */
        public void add(MessageCallback callback, RequestMessage message, 
                long timeout, TimeUnit unit) {
            
            final MessageId messageId = message.getMessageId();
            
            synchronized (callbacks) {
                
                if (!open) {
                    throw new IllegalStateException();
                }
                
                if (callbacks.containsKey(messageId)) {
                    throw new IllegalArgumentException("messageId=" + messageId);
                }
                
                Runnable task = new Runnable() {
                    @Override
                    public void run() {
                        MessageEntity entity 
                            = callbacks.remove(messageId);
                        
                        if (entity != null) {
                            try {
                                entity.handleTimeout();
                            } catch (IOException err) {
                                LOG.error("IOException", err);
                            }
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
        
        /**
         * 
         */
        public MessageEntity get(ResponseMessage message) {
            return callbacks.remove(message.getMessageId());
        }
    }

    /**
     * 
     */
    private static class MessageEntity implements Closeable {
        
        private final long creationTime = System.currentTimeMillis();
        
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
            done.set(true);
        }
        
        /**
         * 
         */
        public boolean handleResponse(ResponseMessage response) throws IOException {
            future.cancel(true);
            
            if (!done.getAndSet(true)) {
                long time = System.currentTimeMillis() - creationTime;
                callback.handleResponse(response, time, TimeUnit.MILLISECONDS);
                return true;
            }
            return false;
        }

        /**
         * 
         */
        public void handleTimeout() throws IOException {
            future.cancel(true);
            
            if (!done.getAndSet(true)) {
                long time = System.currentTimeMillis() - creationTime;
                callback.handleTimeout(request, time, TimeUnit.MILLISECONDS);
            }
        }
    }
}
