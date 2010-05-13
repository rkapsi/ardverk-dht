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
import org.ardverk.io.IoUtils;
import org.ardverk.lang.NullArgumentException;
import org.slf4j.Logger;

import com.ardverk.dht.KUID;
import com.ardverk.dht.io.transport.Transport;
import com.ardverk.dht.io.transport.TransportCallback;
import com.ardverk.dht.message.AbstractMessageCodec;
import com.ardverk.dht.message.Message;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.message.MessageId;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.routing.Contact;
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
    
    private final TransportCallback callback 
            = new TransportCallback() {
        @Override
        public void received(SocketAddress src, 
                byte[] message, int offset, int length) throws IOException {
            MessageDispatcher.this.handleMessage(
                    src, message, offset, length);
        }
    };
    
    private final MessageEntityManager entityManager 
        = new MessageEntityManager();
        
    private final MessageFactory factory;
    
    private final AbstractMessageCodec codec;
    
    private final ResponseChecker checker;
    
    private Transport transport = null;
    
    /**
     * 
     */
    public MessageDispatcher(MessageFactory factory, AbstractMessageCodec codec) {
        
        if (factory == null) {
            throw new NullArgumentException("factory");
        }
        
        if (codec == null) {
            throw new NullArgumentException("codec");
        }
        
        this.factory = factory;
        this.codec = codec;
        this.checker = new ResponseChecker(factory, 512);
    }
    
    /**
     * 
     */
    public synchronized boolean isBound() {
        return transport != null;
    }
    
    /**
     * 
     */
    public synchronized void bind(Transport transport) throws IOException {
        if (transport == null) {
            throw new NullArgumentException("transport");
        }
        
        if (isBound()) {
            throw new IOException();
        }
        
        transport.bind(callback);
        this.transport = transport;
    }
    
    /**
     * 
     */
    public synchronized Transport unbind() {
        return unbind(false);
    }
    
    /**
     * 
     */
    private synchronized Transport unbind(boolean close) {
        Transport copy = this.transport;
        
        if (transport != null) {
            transport.unbind();
            
            if (close && transport instanceof Closeable) {
                IoUtils.close((Closeable)transport);
            }
            
            transport = null;
        }
        
        return copy;
    }
    
    /**
     * 
     */
    public synchronized Transport getTransport() {
        return transport;
    }
    
    @Override
    public void close() {
        unbind(true);
        entityManager.close();
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
    public AbstractMessageCodec getMessageCodec() {
        return codec;
    }
    
    /**
     * 
     */
    protected void send(Message message) throws IOException {
        SocketAddress address = message.getAddress();
        byte[] data = codec.encode(message);
        send(address, data);
    }
    
    /**
     * 
     */
    protected void send(SocketAddress dst, byte[] message) throws IOException {
        send(dst, message, 0, message.length);
    }
    
    /**
     * 
     */
    protected void send(SocketAddress dst, byte[] message, 
            int offset, int length) throws IOException {
        
        Transport transport = null;
        synchronized (this) {
            transport = this.transport;
        }
        
        if (transport == null) {
            throw new IOException();
        }
        
        transport.send(dst, message, offset, length);
    }
    
    /**
     * 
     */
    public void send(Contact dst, ResponseMessage message) throws IOException {
        send(message);
    }
    
    /**
     * 
     */
    public void send(MessageCallback callback, 
            Contact dst, RequestMessage message, 
            long timeout, TimeUnit unit) throws IOException {
        
        KUID contactId = dst.getContactId();
        send(callback, contactId, message, timeout, unit);
    }
    
    /**
     * 
     */
    public void send(MessageCallback callback, 
            KUID contactId, RequestMessage request, 
            long timeout, TimeUnit unit) throws IOException {
        
        if (callback != null) {
            RequestEntity entity = new RequestEntity(
                    contactId, request);
            entityManager.add(callback, entity, timeout, unit);
        }
        
        send(request);
    }
    
    /**
     * 
     */
    public void handleMessage(SocketAddress src, 
            byte[] data, int offset, int length) throws IOException {
        Message message = codec.decode(src, data, offset, length);
        handleMessage(message);
    }
    
    /**
     * 
     */
    public void handleMessage(Message message) throws IOException {
        
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
        public long getTime(TimeUnit unit) {
            long time = System.currentTimeMillis() - creationTime;
            return unit.convert(time, TimeUnit.MILLISECONDS);
        }
        
        /**
         * 
         */
        public void handleResponse(ResponseMessage response) throws IOException {
            if (cancel()) {
                long time = getTime(TimeUnit.MILLISECONDS);
                
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
                
                long time = getTime(TimeUnit.MILLISECONDS);
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
                throw new NullArgumentException("factory");
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
            
            Contact contact = response.getContact();
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
