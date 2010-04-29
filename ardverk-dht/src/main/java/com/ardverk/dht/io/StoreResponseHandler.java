package com.ardverk.dht.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncFuture;

import com.ardverk.dht.KUID;
import com.ardverk.dht.entity.DefaultStoreEntity;
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.entity.StoreEntity;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.message.StoreRequest;
import com.ardverk.dht.message.StoreResponse;
import com.ardverk.dht.routing.Contact2;

public class StoreResponseHandler extends AbstractResponseHandler<StoreEntity> {

    private static final int K = 20;
    
    /*private static final NodeEntity QUERY = new NodeEntity() {
        @Override
        public Contact[] getContacts() {
            return null;
        }
        
        @Override
        public int getHop() {
            return 0;
        }

        @Override
        public AsyncFuture<StoreEntity> store(KUID key, byte[] value) {
            return null;
        }

        @Override
        public long getTime(TimeUnit unit) {
            return 0;
        }

        @Override
        public long getTimeInMillis() {
            return 0;
        }
    };*/
    
    private final ProcessCounter counter = new ProcessCounter(K / 4);
    
    private final StoreManager storeManager;
    
    private final KUID key;
    
    private final byte[] value;
    
    private final long timeout;
    
    private final TimeUnit unit;
    
    private final List<StoreResponse> responses 
        = new ArrayList<StoreResponse>();
    
    private long startTime = -1L;
    
    private int total = 0;
    
    public StoreResponseHandler(
            MessageDispatcher messageDispatcher, 
            NodeEntity entity, KUID key, byte[] value) {
        this(messageDispatcher, entity, key, value, 10L, TimeUnit.SECONDS);
    }
    
    public StoreResponseHandler(
            MessageDispatcher messageDispatcher, 
            NodeEntity entity, KUID key, byte[] value, 
            long timeout, TimeUnit unit) {
        super(messageDispatcher);
        
        if (key == null) {
            throw new NullPointerException("key");
        }
        
        if (value == null) {
            throw new NullPointerException("value");
        }
        
        if (timeout < 0L) {
            throw new IllegalArgumentException("timeout=" + timeout);
        }
        
        if (unit == null) {
            throw new NullPointerException("unit");
        }
        
        this.storeManager = new StoreManager(entity);
        this.key = key;
        this.value = value;
        this.timeout = timeout;
        this.unit = unit;
    }

    @Override
    protected void go(AsyncFuture<StoreEntity> future) throws Exception {
        process(0);
    }

    private synchronized void process(int pop) throws IOException {
        try {
            preProcess(pop);
            
            while (counter.hasNext() && counter.getCount() < K) {
                if (!storeManager.hasNext()) {
                    break;
                }
                
                Contact2 contact = storeManager.next();
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
                setValue(new DefaultStoreEntity(time, TimeUnit.MILLISECONDS));
            }
        }
    }
    
    private synchronized void store(Contact2 dst) throws IOException {
        MessageFactory factory = messageDispatcher.getMessageFactory();
        StoreRequest request = factory.createStoreRequest(dst, key, value);
        
        long adaptiveTimeout = dst.getAdaptiveTimeout(timeout, unit);
        send(dst, request, adaptiveTimeout, unit);
    }
    
    @Override
    protected synchronized void processResponse(RequestMessage request, 
            ResponseMessage response, long time, TimeUnit unit) throws IOException {
        StoreResponse message = (StoreResponse)response;
        
        try {
            responses.add(message);
        } finally {
            process(1);
        }
    }

    @Override
    protected synchronized void processTimeout(RequestMessage request, 
            long time, TimeUnit unit) throws IOException {
        process(1);
    }
    
    private static class StoreManager {
        
        private final NodeEntity entity;
        
        private int index = 0;
        
        public StoreManager(NodeEntity entity) {
            if (entity == null) {
                throw new NullPointerException("entity");
            }
            
            this.entity = entity;
        }
        
        public boolean hasNext() {
            return index < entity.size();
        }
        
        public Contact2 next() {
            return entity.getContact(index++);
        }
    }
}
