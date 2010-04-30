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

import org.ardverk.collection.FixedSizeHashSet;
import org.ardverk.concurrent.ExecutorUtils;
import org.ardverk.lang.NullArgumentException;
import org.slf4j.Logger;

import com.ardverk.dht.KUID;
import com.ardverk.dht.io.transport.Transport;
import com.ardverk.dht.io.transport.TransportListener;
import com.ardverk.dht.message.Message;
import com.ardverk.dht.message.MessageCodec;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.message.MessageId;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.routing.Contact2;
import com.ardverk.logging.LoggerUtils;

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
    
    private final MessageEntityManager entityManager 
        = new MessageEntityManager();
    
    private final Transport transport;
    
    private final MessageFactory factory;
    
    private final MessageCodec codec;
    
    private final ResponseChecker checker;
    
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
        this.checker = new ResponseChecker(factory, 512);
        
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
    public void send(Contact2 dst, ResponseMessage message) throws IOException {
        byte[] data = codec.encode(message);
        
        SocketAddress addr = dst.getContactAddress();
        transport.send(addr, data);
    }
    
    /**
     * 
     */
    public void send(MessageCallback callback, 
            Contact2 dst, RequestMessage message, 
            long timeout, TimeUnit unit) throws IOException {
        
        KUID contactId = dst.getContactId();
        SocketAddress addr = dst.getContactAddress();
        
        send(callback, contactId, addr, message, timeout, unit);
    }
    
    /**
     * 
     */
    public void send(MessageCallback callback, 
            KUID contactId, SocketAddress address, 
            RequestMessage request, long timeout, 
            TimeUnit unit) throws IOException {
        
        byte[] data = codec.encode(request);
        
        RequestEntity entity = new RequestEntity(
                contactId, address, request);
        
        entityManager.add(callback, entity, timeout, unit);
        transport.send(address, data);
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
        
        if (!checker.check(response)) {
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
            RequestEntity entity, ResponseMessage response, 
            long time, TimeUnit unit) throws IOException {
        callback.handleResponse(entity, response, time, unit);
    }
    
    /**
     * 
     */
    protected void handleTimeout(MessageCallback callback, 
            RequestEntity entity, long time, TimeUnit unit) 
                throws IOException {
        callback.handleTimeout(entity, time, unit);
    }
    
    /**
     * 
     */
    protected void handleIllegalResponse(MessageCallback callback, 
            RequestEntity entity, ResponseMessage response, 
            long time, TimeUnit unit) throws IOException {
        
        if (LOG.isErrorEnabled()) {
            LOG.error("Illegal Response: " + entity + " -> " + response);
        }
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
        public void add(MessageCallback callback, RequestEntity entity, 
                long timeout, TimeUnit unit) {
            
            final MessageId messageId = entity.getMessageId();
            
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
                
                MessageEntity entity2 = new MessageEntity(
                        future, callback, entity);
                callbacks.put(messageId, entity2);
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
        
        private final RequestEntity entity;
        
        private final AtomicBoolean open = new AtomicBoolean(true);
        
        private MessageEntity(ScheduledFuture<?> future, 
                MessageCallback callback, 
                RequestEntity entity) {
            
            if (future == null) {
                throw new NullArgumentException("future");
            }
            
            if (callback == null) {
                throw new NullArgumentException("callback");
            }
            
            if (entity == null) {
                throw new NullArgumentException("entity");
            }
            
            this.future = future;
            this.callback = callback;
            this.entity = entity;
        }

        public boolean cancel() {
            future.cancel(true);
            return open.getAndSet(false);
        }
        
        
        /**
         * 
         */
        public void handleResponse(ResponseMessage response) throws IOException {
            if (cancel()) {
                long time = System.currentTimeMillis() - creationTime;
                
                if (entity.check(response)) {
                    MessageDispatcher.this.handleResponse(callback, entity, 
                            response, time, TimeUnit.MILLISECONDS);
                } else {
                    MessageDispatcher.this.handleIllegalResponse(callback, 
                            entity, response, time, TimeUnit.MILLISECONDS);
                }
            }
        }

        /**
         * 
         */
        public void handleTimeout() throws IOException {
            if (cancel()) {
                
                long time = System.currentTimeMillis() - creationTime;
                MessageDispatcher.this.handleTimeout(callback, entity, 
                        time, TimeUnit.MILLISECONDS);
            }
        }
    }
    
    private static class ResponseChecker {
        
        private static final Logger LOG 
            = LoggerUtils.getLogger(ResponseChecker.class);
        
        private final MessageFactory factory;
        
        private final Set<MessageId> history;
        
        public ResponseChecker(MessageFactory factory, int historySize) {
            if (factory == null) {
                throw new NullPointerException("factory");
            }
            
            this.factory = factory;
            this.history = Collections.synchronizedSet(
                    new FixedSizeHashSet<MessageId>(historySize));
        }
        
        public boolean check(ResponseMessage response) {
            MessageId messageId = response.getMessageId();
            if (!history.add(messageId)) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Multiple respones: " + response);
                }
                return false;
            }
            
            Contact2 contact = response.getContact();
            if (!factory.isFor(messageId, contact.getRemoteAddress())) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Wrong MessageId signature: " + response);
                }
                return false;
            }
            
            return true;
        }
    }
}
