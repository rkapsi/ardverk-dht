package com.ardverk.dht.io;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncFuture;

import com.ardverk.dht.KUID;
import com.ardverk.dht.entity.DefaultNodeEntity;
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.message.NodeRequest;
import com.ardverk.dht.message.NodeResponse;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.RouteTable;
import com.ardverk.dht.utils.XorComparator;

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
            Contact[] contacts = lookupManager.getContacts();
            long time = lookupManager.getTimeInMillis();
            
            if (contacts.length == 0) {
                setException(new IOException());                
            } else {
                setValue(new DefaultNodeEntity(
                        contacts, time, TimeUnit.MILLISECONDS));
            }
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
        
        private final NavigableMap<KUID, Contact> respones;
        
        private final QueryPath query;
        
        private final TimeCounter responseCounter = new TimeCounter();
        
        private final TimeCounter timeoutCounter = new TimeCounter();
        
        public LookupManager(RouteTable routeTable, KUID key) {
            if (routeTable == null) {
                throw new NullPointerException("routeTable");
            }
            
            if (key == null) {
                throw new NullPointerException("key");
            }
            
            this.respones = new TreeMap<KUID, Contact>(
                    new XorComparator(key));
            
            this.query = new QueryPath(key);
            
            this.routeTable = routeTable;
            this.key = key;
            
            Contact[] contacts = routeTable.select(key);
            for (Contact contact : contacts) {
                query.add(contact);
            }
        }
        
        public void handleResponse(NodeResponse response, 
                long time, TimeUnit unit) {
            responseCounter.addTime(time, unit);
            
            Contact src = response.getContact();
            respones.put(src.getContactId(), src);
            
            Contact[] contacts = response.getContacts();
            for (Contact contact : contacts) {
                query.add(contact);
            }
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
        
        public Contact[] getContacts() {
            return respones.values().toArray(new Contact[0]);
        }
        
        private boolean isCloserThanClosest(Contact contact) {
            Map.Entry<KUID, Contact> entry = respones.firstEntry();
            if (entry == null) {
                return true;
            }
            
            KUID contactId = contact.getContactId();
            KUID closestId = entry.getKey();
            
            return contactId.isCloserTo(key, closestId);
        }
        
        public boolean hasNext() {
            Contact contact = query.get();
            
            if (contact != null && isCloserThanClosest(contact)) {
                return true;
            }
            
            return false;
        }
        
        public Contact next() {
            Contact contact = query.poll();
            if (contact == null) {
                throw new NoSuchElementException();
            }
            return contact;
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
    
    private static class QueryPath {
        
        private final Set<KUID> history = new HashSet<KUID>();
        
        private final NavigableMap<KUID, Contact> query;
        
        public QueryPath(KUID key) {
            query = new TreeMap<KUID, Contact>(new XorComparator(key));
        }
        
        public boolean contains(Contact contact) {
            return history.contains(contact.getContactId());
        }
        
        public boolean add(Contact contact) {
            KUID contactId = contact.getContactId();
            
            if (history.add(contactId)) {
                query.put(contactId, contact);
                return true;
            }
            
            return false; 
        }
        
        public Contact get() {
            Map.Entry<KUID, Contact> entry = query.firstEntry();
            return entry != null ? entry.getValue() : null;
        }
        
        public Contact poll() {
            Map.Entry<KUID, Contact> entry = query.pollFirstEntry();
            return entry != null ? entry.getValue() : null;
        }
        
        @Override
        public String toString() {
            return query.keySet().toString();
        }
    }
    
    /*public static void main(String[] args) {
        KUID key = new KUID(new byte[] { 1, 2, 3 });
        System.out.println(key);
        
        QueryPath query = new QueryPath(key);
        Map<KUID, KUID> foo = new TreeMap<KUID, KUID>();
        
        Random generator = new Random();
        
        for (int i = 0; i < 100; i++) {
            byte[] contactId = new byte[3];
            generator.nextBytes(contactId);
            KUID bla = new KUID(contactId);
            
            query.add(new DefaultContact(Type.SOLICITED, 
                    bla, 0, 
                    new InetSocketAddress("localhost", 6666), 
                    new InetSocketAddress("localhost", 6666)));
            
            foo.put(key.xor(bla), bla);
            System.out.println("XOR: " + key.xor(bla));
        }
        
        System.out.println(foo.keySet());
        System.out.println(foo.values());
        System.out.println(query);
        
        System.out.println();
        System.out.println(query.get());
        
        query.add(new DefaultContact(Type.SOLICITED, 
                key, 0, 
                new InetSocketAddress("localhost", 6666), 
                new InetSocketAddress("localhost", 6666)));
        
        System.out.println(query.get());
    }*/
}
