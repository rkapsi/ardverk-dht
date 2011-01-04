/*
 * Copyright 2009-2010 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ardverk.dht.io;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.ardverk.collection.FixedSizeHashSet;
import org.ardverk.concurrent.ExecutorUtils;
import org.ardverk.io.Bindable;
import org.ardverk.io.IoUtils;
import org.ardverk.lang.Arguments;
import org.ardverk.lang.NullArgumentException;
import org.ardverk.lang.TimeStamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ardverk.dht.KUID;
import com.ardverk.dht.codec.MessageCodec;
import com.ardverk.dht.event.EventUtils;
import com.ardverk.dht.io.transport.Transport;
import com.ardverk.dht.io.transport.TransportCallback;
import com.ardverk.dht.message.Message;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.message.MessageId;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.routing.Contact;

/**
 * The {@link MessageDispatcher} is responsible for sending messages over
 * a given {@link Transport} and keeping track of the messages.
 */
public abstract class MessageDispatcher 
        implements Bindable<Transport>, Closeable {
    
    private static final Logger LOG 
        = LoggerFactory.getLogger(MessageDispatcher.class);
    
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
    
    private final List<MessageListener> listeners 
        = new CopyOnWriteArrayList<MessageListener>();
    
    private final MessageEntityManager entityManager 
        = new MessageEntityManager();
        
    private final ScheduledExecutorService executor;
    
    private final MessageFactory factory;
    
    private final MessageCodec codec;
    
    private final ResponseChecker checker;
    
    private Transport transport = null;
    
    /**
     * Creates a {@link MessageDispatcher}.
     */
    public MessageDispatcher(MessageFactory factory, MessageCodec codec) {
        this(EXECUTOR, factory, codec);
    }
    
    /**
     * Creates a {@link MessageDispatcher} with a custom 
     * {@link ScheduledExecutorService} that is used to
     * keep for timing out requests.
     */
    public MessageDispatcher(ScheduledExecutorService executor, 
            MessageFactory factory, MessageCodec codec) {
        this.executor = Arguments.notNull(executor, "executor");
        this.factory = Arguments.notNull(factory, "factory");
        this.codec = Arguments.notNull(codec, "codec");
        
        // TODO: Is memorizing the 512 most recently received MessageIds
        // too much or too little? 
        this.checker = new ResponseChecker(factory, 512);
    }
    
    /**
     * Returns the {@link Transport}
     */
    public synchronized Transport getTransport() {
        return transport;
    }
    
    @Override
    public synchronized boolean isBound() {
        return transport != null;
    }
    
    @Override
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
    
    @Override
    public synchronized void unbind() {
        unbind(false);
    }
    
    /**
     * Unbinds the {@link Transport} and closes it optionally.
     */
    private synchronized void unbind(boolean close) {
        if (transport != null) {
            IoUtils.unbind(transport);
            
            if (close) {
                IoUtils.close(transport);
            }
            
            transport = null;
        }
    }
    
    @Override
    public void close() {
        unbind(true);
        entityManager.close();
    }
    
    /**
     * Returns the {@link MessageFactory}
     */
    public MessageFactory getMessageFactory() {
        return factory;
    }
    
    /**
     * Returns the {@link MessageCodec}
     */
    public MessageCodec getMessageCodec() {
        return codec;
    }
    
    /**
     * Sends the given {@link Message}.
     */
    protected void send(Message message) throws IOException {
        SocketAddress address = message.getAddress();
        byte[] data = codec.encode(message);
        send(address, data);
    }
    
    /**
     * Sends the given bytes (message) to the given {@link SocketAddress}.
     */
    protected void send(SocketAddress dst, byte[] message) throws IOException {
        send(dst, message, 0, message.length);
    }
    
    /**
     * Sends the given bytes (message) to the given {@link SocketAddress}.
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
     * Sends a {@link ResponseMessage} to the given {@link Contact}.
     */
    public void send(Contact dst, ResponseMessage message) throws IOException {
        send(message);
        fireMessageSent(dst, message);
    }
    
    /**
     * Sends a {@link RequestMessage} to the given {@link Contact}.
     */
    public void send(MessageCallback callback, 
            Contact dst, RequestMessage message, 
            long timeout, TimeUnit unit) throws IOException {
        
        KUID contactId = dst.getId();
        send(callback, contactId, message, timeout, unit);
    }
    
    /**
     * Sends a {@link RequestMessage} to the a {@link Contact} with the 
     * given {@link KUID}.
     * 
     * NOTE: The destination {@link SocketAddress} is encoded in the 
     * {@link RequestMessage}. The {@link KUID} is used to validate
     * {@link ResponseMessage}s.
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
        fireMessageSent(contactId, request);
    }
    
    /**
     * Callback method for incoming {@link Message}s.
     */
    public void handleMessage(SocketAddress src, 
            byte[] data, int offset, int length) throws IOException {
        Message message = codec.decode(src, data, offset, length);
        handleMessage(message);
        fireMessageReceived(message);
    }
    
    /**
     * Callback method for incoming {@link Message}s.
     */
    public void handleMessage(Message message) throws IOException {
        if (message instanceof RequestMessage) {
            handleRequest((RequestMessage)message);
        } else {
            handleResponse((ResponseMessage)message);
        }
    }
    
    /**
     * Callback method for incoming {@link ResponseMessage}s.
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
     * Callback method for incoming {@link RequestMessage}s.
     */
    protected abstract void handleRequest(RequestMessage request) throws IOException;
    
    /**
     * Callback method for late incoming {@link ResponseMessage}s.
     */
    protected abstract void lateResponse(ResponseMessage response) throws IOException;
    
    /**
     * Callback method for late incoming {@link ResponseMessage}s.
     */
    protected void handleResponse(MessageCallback callback, 
            RequestEntity entity, ResponseMessage response, 
            long time, TimeUnit unit) throws IOException {
        callback.handleResponse(entity, response, time, unit);
    }
    
    /**
     * Callback method for timeouts.
     */
    protected void handleTimeout(MessageCallback callback, 
            RequestEntity entity, long time, TimeUnit unit) 
                throws IOException {
        callback.handleTimeout(entity, time, unit);
    }
    
    /**
     * Callback method for illegal {@link ResponseMessage}s.
     */
    protected void handleIllegalResponse(MessageCallback callback, 
            RequestEntity entity, ResponseMessage response, 
            long time, TimeUnit unit) throws IOException {
        
        if (LOG.isErrorEnabled()) {
            LOG.error("Illegal Response: " + entity + " -> " + response);
        }
    }
    
    /**
     * Adds the given {@link MessageListener}.
     */
    public void addMessageListener(MessageListener l) {
        listeners.add(Arguments.notNull(l, "l"));
    }
    
    /**
     * Removes the given {@link MessageListener}.
     */
    public void removeMessageListener(MessageListener l) {
        listeners.remove(l);
    }
    
    /**
     * Returns all {@link MessageListener}s.
     */
    public MessageListener[] getMessageListeners() {
        return listeners.toArray(new MessageListener[0]);
    }
    
    /**
     * Fires a message sent event.
     */
    protected void fireMessageSent(Contact dst, Message message) {
        fireMessageSent(dst.getId(), message);
    }
    
    /**
     * Fires a message sent event.
     */
    protected void fireMessageSent(final KUID contactId, final Message message) {
        
        if (!listeners.isEmpty()) {
            Runnable event = new Runnable() {
                @Override
                public void run() {
                    for (MessageListener l : listeners) {
                        l.handleMessageSent(contactId, message);
                    }
                }
            };
            EventUtils.fireEvent(event);
        }
    }
    
    /**
     * Fires a message received event.
     */
    protected void fireMessageReceived(final Message message) {
        if (!listeners.isEmpty()) {
            Runnable event = new Runnable() {
                @Override
                public void run() {
                    for (MessageListener l : listeners) {
                        l.handleMessageReceived(message);
                    }
                }
            };
            EventUtils.fireEvent(event);
        }
    }
    
    /**
     * The {@link MessageEntityManager} keeps track of {@link RequestEntity}s
     * and their {@link MessageCallback}s. It's also responsible for timing
     * out {@link RequestMessage} for which we haven't received any responses
     * within a defined time frame.
     */
    private class MessageEntityManager implements Closeable {
        
        private final Map<MessageId, MessageEntity> callbacks 
            = Collections.synchronizedMap(new HashMap<MessageId, MessageEntity>());
        
        private boolean open = true;
        
        @Override
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
         * Adds a {@link RequestEntity} and its {@link MessageCallback}.
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
                    = executor.schedule(task, timeout, unit);
                
                MessageEntity messageEntity = new MessageEntity(
                        future, callback, entity);
                callbacks.put(messageId, messageEntity);
            }
        }
        
        /**
         * Returns a {@link MessageEntity} for the given {@link ResponseMessage}.
         */
        public MessageEntity get(ResponseMessage message) {
            return callbacks.remove(message.getMessageId());
        }
    }

    /**
     * A {@link MessageEntity} represents a {@link RequestMessage} we've sent 
     * and provides the infrastructure to process the {@link ResponseMessage}.
     */
    private class MessageEntity {
        
        private final TimeStamp creationTime = TimeStamp.now();
        
        private final ScheduledFuture<?> future;

        private final MessageCallback callback;
        
        private final RequestEntity entity;
        
        private final AtomicBoolean open = new AtomicBoolean(true);
        
        private MessageEntity(ScheduledFuture<?> future, 
                MessageCallback callback, 
                RequestEntity entity) {
            
            this.future = Arguments.notNull(future, "future");
            this.callback = Arguments.notNull(callback, "callback");
            this.entity = Arguments.notNull(entity, "entity");
        }

        /**
         * Cancels the {@link MessageEntity}.
         */
        public boolean cancel() {
            future.cancel(true);
            return open.getAndSet(false);
        }
        
        /**
         * Called if a {@link ResponseMessage} was received.
         */
        public void handleResponse(ResponseMessage response) throws IOException {
            if (cancel()) {
                long time = creationTime.getAgeInMillis();
                
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
         * Called if a timeout occurred (i.e. we didn't receive a 
         * {@link ResponseMessage} within in the predefined time).
         */
        public void handleTimeout() throws IOException {
            if (cancel()) {
                
                long time = creationTime.getAgeInMillis();
                MessageDispatcher.this.handleTimeout(callback, entity, 
                        time, TimeUnit.MILLISECONDS);
            }
        }
    }
    
    /**
     * The {@link ResponseChecker} makes sure {@link ResponseMessage}s fulfill
     * certain requirements before they're considered for further processing.
     */
    private static class ResponseChecker {
        
        private static final Logger LOG 
            = LoggerFactory.getLogger(ResponseChecker.class);
        
        private final MessageFactory factory;
        
        private final Set<MessageId> history;
        
        public ResponseChecker(MessageFactory factory, int historySize) {
            this.factory = Arguments.notNull(factory, "factory");
            this.history = Collections.synchronizedSet(
                    new FixedSizeHashSet<MessageId>(historySize));
        }
        
        /**
         * Returns {@code true} if the {@link ResponseMessage} is OK.
         */
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