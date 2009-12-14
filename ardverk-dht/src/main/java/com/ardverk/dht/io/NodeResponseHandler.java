package com.ardverk.dht.io;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncFuture;

import com.ardverk.dht.KUID;
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.RouteTable;

public class NodeResponseHandler extends ResponseHandler<NodeEntity> {
    
    private static final int K = 20;
    
    private static final int ALPHA = 4;
    
    private final LookupManager lookupManager;
    
    private final LookupCounter lookupCounter;
    
    private final int k = K;
    
    public NodeResponseHandler(MessageDispatcher messageDispatcher, 
            RouteTable routeTable, KUID key) {
        super(messageDispatcher);
        
        if (k < 0) {
            throw new IllegalArgumentException("k=" + k);
        }
        
        lookupManager = new LookupManager(routeTable, key);
        lookupCounter = new LookupCounter(ALPHA);
    }

    @Override
    protected void go(AsyncFuture<NodeEntity> future)
            throws Exception {
        process(0);
    }
    
    private synchronized void process(int pop) {
        try {
            preProcess(pop);
            while (lookupCounter.hasNext()) {
                if (!lookupManager.hasNext()) {
                    break;
                }
                
                // TODO: lookup
                Contact contact = lookupManager.next();
                lookupCounter.push();
            }
        } finally {
            postProcess();
        }
    }
    
    private synchronized void preProcess(int pop) {
        while (0 < pop--) {
            lookupCounter.pop();
        }
    }
    
    private synchronized void postProcess() {
        int count = lookupCounter.count();
        if (count == 0) {
            setException(new IOException());
        }
    }

    @Override
    protected synchronized void processResponse(RequestMessage request,
            ResponseMessage response, long time, TimeUnit unit)
            throws IOException {
        
        try {
            lookupManager.handleResponse(response, time, unit);
        } finally {
            process(1);
        }
    }

    @Override
    protected synchronized void processTimeout(RequestMessage request, 
            long time, TimeUnit unit) throws IOException {
        
        process(1);
    }
    
    private static class LookupManager {
        
        private final RouteTable routeTable;
        
        private final KUID key;
        
        private long time = 0L;
        
        public LookupManager(RouteTable routeTable, KUID key) {
            if (routeTable == null) {
                throw new NullPointerException("routeTable");
            }
            
            if (key == null) {
                throw new NullPointerException("key");
            }
            
            this.routeTable = routeTable;
            this.key = key;
        }
        
        public void handleResponse(ResponseMessage response, long time, TimeUnit unit) {
            this.time += unit.toMillis(time);
            
            // TODO process response
        }
        
        public boolean hasNext() {
            return false;
        }
        
        public Contact next() {
            return null;
        }
        
        public Contact[] select(int count) {
            return routeTable.select(key, count);
        }
        
        public long getTime(TimeUnit unit) {
            return unit.convert(time, TimeUnit.MILLISECONDS);
        }
        
        public long getTimeInMillis() {
            return getTime(TimeUnit.MILLISECONDS);
        }
    }
    
    private static class LookupCounter {
        
        private final int max;
        
        private int counter = 0;
        
        public LookupCounter(int max) {
            if (max < 0) {
                throw new IllegalArgumentException("max=" + max);
            }
            
            this.max = max;
        }
        
        public boolean hasNext() {
            return counter < max;
        }
        
        public boolean push() {
            if (counter < max) {
                ++counter;
                return true;
            }
            return false;
        }
        
        public void pop() {
            if (0 < counter) {
                --counter;
            }
        }
        
        public int count() {
            return counter;
        }
    }
}
