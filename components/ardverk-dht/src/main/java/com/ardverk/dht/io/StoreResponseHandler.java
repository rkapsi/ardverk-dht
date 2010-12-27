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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.ardverk.collection.Iterators;
import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.lang.Arguments;

import com.ardverk.dht.config.StoreConfig;
import com.ardverk.dht.entity.DefaultStoreEntity;
import com.ardverk.dht.entity.StoreEntity;
import com.ardverk.dht.lang.TimeStamp;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.message.StoreRequest;
import com.ardverk.dht.message.StoreResponse;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.storage.ValueTuple;

/**
 * The {@link StoreResponseHandler} manages the STORE process.
 */
public class StoreResponseHandler extends AbstractResponseHandler<StoreEntity> {
    
    private final ProcessCounter counter;
    
    private final List<StoreResponse> responses 
        = new ArrayList<StoreResponse>();

    private final TimeStamp creationTime = TimeStamp.now();
    
    private final Iterator<Contact> contacts;
    
    private final int k;
    
    private final ValueTuple tuple;
    
    private final StoreConfig config;
    
    public StoreResponseHandler(
            MessageDispatcher messageDispatcher, 
            Contact[] contacts, int k,
            ValueTuple tuple, StoreConfig config) {
        super(messageDispatcher);
        
        this.contacts = Iterators.fromArray(contacts);
        this.k = k;
        
        this.tuple = Arguments.notNull(tuple, "tuple");
        this.config = Arguments.notNull(config, "config");
        
        counter = new ProcessCounter(config.getS());
    }

    @Override
    protected void go(AsyncFuture<StoreEntity> future) throws Exception {
        process(0);
    }

    private synchronized void process(int pop) throws IOException {
        try {
            preProcess(pop);
            
            while (counter.hasNext() && counter.getCount() < k) {
                if (!contacts.hasNext()) {
                    break;
                }
                
                Contact contact = contacts.next();
                store(contact);
                
                counter.increment();
            }
            
        } finally {
            postProcess();
        }
    }
    
    private synchronized void preProcess(int pop) {
        while (0 < pop--) {
            counter.decrement();
        }
    }
    
    private synchronized void postProcess() {
        if (counter.getProcesses() == 0) {
            long time = creationTime.getAgeInMillis();
            
            StoreResponse[] values = responses.toArray(new StoreResponse[0]);
            if (values.length == 0) {
                setException(new StoreException(tuple, time, TimeUnit.MILLISECONDS));
            } else {
                setValue(new DefaultStoreEntity(values, 
                        time, TimeUnit.MILLISECONDS));
            }
        }
    }
    
    private synchronized void store(Contact dst) throws IOException {
        MessageFactory factory = messageDispatcher.getMessageFactory();
        StoreRequest request = factory.createStoreRequest(dst, tuple);
        
        long defaultTimeout = config.getStoreTimeoutInMillis();
        long adaptiveTimeout = config.getAdaptiveTimeout(
                dst, defaultTimeout, TimeUnit.MILLISECONDS);
        
        send(dst, request, adaptiveTimeout, TimeUnit.MILLISECONDS);
    }
    
    @Override
    protected synchronized void processResponse(RequestEntity entity, 
            ResponseMessage response, long time, TimeUnit unit) throws IOException {
        StoreResponse message = (StoreResponse)response;
        
        try {
            responses.add(message);
        } finally {
            process(1);
        }
    }

    @Override
    protected synchronized void processTimeout(RequestEntity entity, 
            long time, TimeUnit unit) throws IOException {
        process(1);
    }
}