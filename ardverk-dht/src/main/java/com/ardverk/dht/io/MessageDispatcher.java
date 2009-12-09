package com.ardverk.dht.io;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;

import com.ardverk.collection.FixedSizeHashSet;
import com.ardverk.dht.io.transport.Transport;
import com.ardverk.dht.io.transport.TransportListener;
import com.ardverk.dht.message.Message;
import com.ardverk.dht.message.MessageCodec;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.message.MessageId;
import com.ardverk.dht.message.PingRequest;
import com.ardverk.dht.message.PingResponse;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.routing.Contact;
import com.ardverk.logging.LoggerUtils;
import com.ardverk.utils.ExecutorUtils;

/**
 * 
 */
public abstract class MessageDispatcher implements Closeable {
    
    private static final Logger LOG 
        = LoggerUtils.getLogger(MessageDispatcher.class);
    
    private static final ScheduledExecutorService EXECUTOR 
        = ExecutorUtils.newSingleThreadScheduledExecutor(
            "MessageDispatcherThread");
    
    private final TransportListener listener 
            = new TransportListener() {
        @Override
        public void received(SocketAddress src, 
                byte[] message) throws IOException {
            MessageDispatcher.this.handleMessage(src, message);
        }
    };
    
    private final Set<MessageId> history 
        = new FixedSizeHashSet<MessageId>(512);
    
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
        Message message = codec.decode(src, data);
        handleMessage(message);
    }
    
    /**
     * 
     */
    private void handleMessage(Message message) throws IOException {
        
        if (message instanceof RequestMessage) {
            handleRequest((RequestMessage)message);
        } else {
            handleResponse((ResponseMessage)message);
        }
    }
    
    /**
     * 
     */
    private void handleResponse(ResponseMessage response) throws IOException {
        
        MessageId messageId = response.getMessageId();
        if (!history.add(messageId)) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Multiple respones: " + response);
            }
            return;
        }
        
        Contact contact = response.getContact();
        if (!factory.isFor(messageId, contact.getAddress())) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Wrong MessageId signature: " + response);
            }
            return;
        }
        
        MessageEntity entity = entityManager.get(response);
        if (entity != null) {
            entity.handleResponse(response);
        } else {
            lateResponse(response);
        }
    }
    
    /**
     * 
     */
    protected abstract void handleRequest(RequestMessage request) throws IOException;
    
    /**
     * 
     */
    protected abstract void lateResponse(ResponseMessage response) throws IOException;
    
    /**
     * 
     */
    protected void handleResponse(MessageCallback callback, 
            RequestMessage request, ResponseMessage response, 
            long time, TimeUnit unit) throws IOException {
        callback.handleResponse(request, response, time, unit);
    }
    
    /**
     * 
     */
    protected void handleTimeout(MessageCallback callback, 
            RequestMessage request, long time, TimeUnit unit) 
                throws IOException {
        callback.handleTimeout(request, time, unit);
    }
    
    /**
     * 
     */
    private class MessageEntityManager implements Closeable {
        
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
                    entity.cancel();
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
    private class MessageEntity {
        
        private final long creationTime = System.currentTimeMillis();
        
        private final ScheduledFuture<?> future;

        private final MessageCallback callback;
        
        private final RequestMessage request;
        
        private final AtomicBoolean open = new AtomicBoolean(true);
        
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

        public boolean cancel() {
            future.cancel(true);
            return open.getAndSet(false);
        }
        
        /**
         * 
         */
        public boolean check(ResponseMessage response) {
            if (request instanceof PingRequest) {
                return response instanceof PingResponse;
            }
            
            return false;
        }
        
        /**
         * 
         */
        public void handleResponse(ResponseMessage response) throws IOException {
            if (cancel() && check(response)) {
                long time = System.currentTimeMillis() - creationTime;
                MessageDispatcher.this.handleResponse(callback, request, 
                        response, time, TimeUnit.MILLISECONDS);
            }
        }

        /**
         * 
         */
        public void handleTimeout() throws IOException {
            if (cancel()) {
                long time = System.currentTimeMillis() - creationTime;
                MessageDispatcher.this.handleTimeout(callback, request, 
                        time, TimeUnit.MILLISECONDS);
            }
        }
    }
}
