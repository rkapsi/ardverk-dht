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
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.entity.StoreEntity;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.message.StoreRequest;
import com.ardverk.dht.message.StoreResponse;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.storage.ValueTuple;

public class StoreResponseHandler extends AbstractResponseHandler<StoreEntity> {

    public static final NodeEntity DEFAULT = null;
    
    private final ProcessCounter counter;
    
    private final List<StoreResponse> responses 
        = new ArrayList<StoreResponse>();

    private final Iterator<Contact> contacts;
    
    private final int k;
    
    private final ValueTuple tuple;
    
    private final StoreConfig config;
    
    private long startTime = -1L;
    
    private int total = 0;
    
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
        if (startTime == -1L) {
            startTime = System.currentTimeMillis();
        }
        
        while (0 < pop--) {
            counter.decrement();
        }
    }
    
    private synchronized void postProcess() {
        if (counter.getProcesses() == 0) {
            StoreResponse[] values = responses.toArray(new StoreResponse[0]);
            if (values.length == 0) {
                setException(new IOException());
            } else {
                long time = System.currentTimeMillis() - startTime;
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
