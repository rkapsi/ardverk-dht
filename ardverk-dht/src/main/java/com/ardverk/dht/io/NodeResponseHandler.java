package com.ardverk.dht.io;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncFuture;

import com.ardverk.dht.KUID;
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.message.NodeRequest;
import com.ardverk.dht.message.NodeResponse;
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
    
    private final long timeout = 3L;
    
    private final TimeUnit unit = TimeUnit.SECONDS;
    
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
    protected void go(AsyncFuture<NodeEntity> future) throws IOException {
        process(0);
    }
    
    private synchronized void process(int pop) throws IOException {
        try {
            preProcess(pop);
            while (lookupCounter.hasNext()) {
                if (!lookupManager.hasNext()) {
                    break;
                }
                
                Contact contact = lookupManager.next();
                lookup(contact);
                
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
        int count = lookupCounter.getCount();
        if (count == 0) {
            setException(new IOException());
        }
    }
    
    private void lookup(Contact dst) throws IOException {
        MessageFactory factory = messageDispatcher.getMessageFactory();
        NodeRequest message = factory.createNodeRequest(
                dst, lookupManager.key);
        messageDispatcher.send(this, message, timeout, unit);
    }

    @Override
    protected synchronized void processResponse(RequestMessage request,
            ResponseMessage response, long time, TimeUnit unit)
            throws IOException {
        
        System.out.println("RESPONSE: " + response);
        
        try {
            lookupManager.handleResponse((NodeResponse)response, time, unit);
        } finally {
            process(1);
        }
    }

    @Override
    protected synchronized void processTimeout(RequestMessage request, 
            long time, TimeUnit unit) throws IOException {
        
        System.out.println("TIMEOUT: " + request);
        
        try {
            lookupManager.handleTimeout(time, unit);
        } finally {
            process(1);
        }
    }
    
    /**
     * 
     */
    private static class LookupManager {
        
        private final RouteTable routeTable;
        
        private final KUID key;
        
        private final TimeCounter responseCounter = new TimeCounter();
        
        private final TimeCounter timeoutCounter = new TimeCounter();
        
        private final Contact[] contacts;
        
        private Contact clostest = null;
        
        private int index = 0;
        
        public LookupManager(RouteTable routeTable, KUID key) {
            if (routeTable == null) {
                throw new NullPointerException("routeTable");
            }
            
            if (key == null) {
                throw new NullPointerException("key");
            }
            
            this.routeTable = routeTable;
            this.key = key;
            
            this.contacts = routeTable.select(key);
        }
        
        public void handleResponse(NodeResponse response, 
                long time, TimeUnit unit) {
            responseCounter.addTime(time, unit);
            
            Contact src = response.getContact();
            
            if (clostest == null || src.getContactId().isCloserTo(
                    key, clostest.getContactId())) {
                clostest = src;
            }
            
            Contact[] contacts = response.getContacts();
            for (Contact contact : contacts) {
                System.out.println(contact);
            }
            // TODO process response
        }
        
        public void handleTimeout(long time, TimeUnit unit) {
            timeoutCounter.addTime(time, unit);
        }
        
        public long getTime(TimeUnit unit) {
            return responseCounter.getTime(unit) 
                    + timeoutCounter.getTime(unit);
        }
        
        public long getTimeInMillis() {
            return getTime(TimeUnit.MILLISECONDS);
        }
        
        public boolean hasNext() {
            return index < contacts.length;
        }
        
        public Contact next() {
            return contacts[index++];
        }
    }
    
    /**
     * 
     */
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
        
        public int getCount() {
            return counter;
        }
    }
    
    /**
     * 
     */
    private static class TimeCounter {
        
        private long time = 0L;
        
        private int count = 0;
        
        public void addTime(long time, TimeUnit unit) {
            this.time += unit.toNanos(time);
            ++count;
        }
        
        public long getTime(TimeUnit unit) {
            return unit.convert(time, TimeUnit.NANOSECONDS);
        }
        
        public long getTimeInMillis() {
            return getTime(TimeUnit.MILLISECONDS);
        }
        
        public int getCount() {
            return count;
        }
        
        @Override
        public String toString() {
            return getTimeInMillis() + " ms @ " + getCount();
        }
    }
}
