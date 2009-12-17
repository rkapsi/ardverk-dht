package com.ardverk.dht.io;

import java.io.IOException;
import java.io.Serializable;
import java.util.Comparator;
import java.util.HashSet;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;
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
        
        //System.out.println("RESPONSE: " + response);
        
        try {
            lookupManager.handleResponse((NodeResponse)response, time, unit);
        } finally {
            process(1);
        }
    }

    @Override
    protected synchronized void processTimeout(RequestMessage request, 
            long time, TimeUnit unit) throws IOException {
        
        //System.out.println("TIMEOUT: " + request);
        
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
        
        private static final boolean EXHAUSTIVE = false;
        
        private final RouteTable routeTable;
        
        private final KUID key;
        
        /**
         * A {@link Set} of all responses
         */
        private final NavigableSet<Contact> responses;
        
        /**
         * A {@link Set} of the k-closest responses
         */
        private final NavigableSet<Contact> closest;
        
        /**
         * A {@link Set} of {@link Contact}s to query
         */
        private final NavigableSet<Contact> query;
        
        private final TimeCounter responseCounter = new TimeCounter();
        
        private final TimeCounter timeoutCounter = new TimeCounter();
        
        public LookupManager(RouteTable routeTable, KUID key) {
            if (routeTable == null) {
                throw new NullPointerException("routeTable");
            }
            
            if (key == null) {
                throw new NullPointerException("key");
            }
            
            Contact localhost = routeTable.getLocalhost();
            KUID contactId = localhost.getContactId();
            
            XorComparator comparator = new XorComparator(key);
            this.responses = new TreeSet<Contact>(comparator);
            this.closest = new TreeSet<Contact>(comparator);
            this.query = new QueryPath(comparator, contactId);
            
            this.routeTable = routeTable;
            this.key = key;
            
            Contact[] contacts = routeTable.select(key);
            
            for (Contact contact : contacts) {
                query.add(contact);
            }
            
            if (!query.isEmpty()) {
                addToResponses(localhost);
            }
        }
        
        public void handleResponse(NodeResponse response, 
                long time, TimeUnit unit) {
            responseCounter.addTime(time, unit);
            
            addToResponses(response.getContact());
            
            Contact[] contacts = response.getContacts();
            for (Contact contact : contacts) {
                if (query.add(contact)) {
                    routeTable.add(contact);
                }
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
            return responses.toArray(new Contact[0]);
        }
        
        private boolean addToResponses(Contact contact) {
            if (responses.add(contact)) {
                closest.add(contact);
                
                if (closest.size() > routeTable.getK()) {
                    closest.pollLast();
                }
                return true;
            }
            return false;
        }
        
        private boolean isCloserThanClosest(Contact other) {
            if (!closest.isEmpty()) {
                Contact contact = closest.last();
                KUID contactId = contact.getContactId();
                KUID otherId = other.getContactId();
                return otherId.isCloserTo(key, contactId);
            }
            
            return true;
        }
        
        public boolean hasNext() {
            if (!query.isEmpty()) {
                Contact contact = query.first();
                if (responses.size() < routeTable.getK() 
                        || isCloserThanClosest(contact) 
                        || EXHAUSTIVE) {
                    return true;
                }
            }
            
            return false;
        }
        
        public Contact next() {
            Contact contact = query.pollFirst();
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
    
    private static class QueryPath extends TreeSet<Contact> {
        
        private static final long serialVersionUID = -8700866833637036503L;
        
        private final Set<KUID> history = new HashSet<KUID>();
        
        public QueryPath(Comparator<? super Contact> c, KUID contactId) {
            super(c);
            
            if (contactId == null) {
                throw new NullPointerException("contactId");
            }
            
            history.add(contactId);
        }
        
        @Override
        public boolean add(Contact contact) {
            KUID contactId = contact.getContactId();
            
            if (history.add(contactId)) {
                super.add(contact);
                return true;
            }
            
            return false; 
        }
    }
    
    /**
     * 
     */
    private static class XorComparator implements Comparator<Contact>, Serializable {
        
        private static final long serialVersionUID = -7543333434594933816L;
        
        private final KUID key;
        
        public XorComparator(KUID key) {
            if (key == null) {
                throw new NullPointerException("key");
            }
            
            this.key = key;
        }
        
        @Override
        public int compare(Contact o1, Contact o2) {
            return o1.getContactId().xor(key).compareTo(o2.getContactId().xor(key));
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
