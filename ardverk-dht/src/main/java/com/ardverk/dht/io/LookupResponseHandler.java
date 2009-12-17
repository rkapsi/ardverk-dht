package com.ardverk.dht.io;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncFuture;

import com.ardverk.dht.KUID;
import com.ardverk.dht.entity.LookupEntity;
import com.ardverk.dht.message.LookupRequest;
import com.ardverk.dht.message.NodeResponse;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.RouteTable;

abstract class LookupResponseHandler<T extends LookupEntity> extends ResponseHandler<T> {
    
    private static final int ALPHA = 4;
    
    private final LookupManager lookupManager;
    
    private final LookupCounter lookupCounter;
    
    private final long timeout = 3L;
    
    private final TimeUnit unit = TimeUnit.SECONDS;
    
    private long startTime = -1L;
    
    public LookupResponseHandler(MessageDispatcher messageDispatcher, 
            RouteTable routeTable, KUID key) {
        this(messageDispatcher, routeTable, key, ALPHA);
    }
    
    public LookupResponseHandler(MessageDispatcher messageDispatcher, 
            RouteTable routeTable, KUID key, int alpha) {
        super(messageDispatcher);
        
        lookupManager = new LookupManager(routeTable, key);
        lookupCounter = new LookupCounter(alpha);
    }

    @Override
    protected void go(AsyncFuture<T> future) throws IOException {
        process(0);
    }
    
    /**
     * 
     */
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
    
    /**
     * 
     */
    private synchronized void preProcess(int pop) {
        if (startTime == -1L) {
            startTime = System.currentTimeMillis();
        }
        
        while (0 < pop--) {
            lookupCounter.pop();
        }
    }
    
    /**
     * 
     */
    private synchronized void postProcess() {
        int count = lookupCounter.getCount();
        if (count == 0) {
            Contact[] contacts = lookupManager.getContacts();
            int currentHop = lookupManager.getCurrentHop();
            long time = System.currentTimeMillis() - startTime;
            
            complete(contacts, currentHop, time, TimeUnit.MILLISECONDS);
        }
    }
    
    /**
     * 
     */
    protected abstract void complete(Contact[] contacts, 
            int hop, long time, TimeUnit unit);
    
    /**
     * 
     */
    protected abstract LookupRequest createLookupRequest(Contact dst, KUID key);
    
    /**
     * 
     */
    private void lookup(Contact dst) throws IOException {
        LookupRequest message = createLookupRequest(dst, lookupManager.key);
        messageDispatcher.send(this, message, timeout, unit);
    }

    @Override
    protected synchronized void processResponse(RequestMessage request,
            ResponseMessage response, long time, TimeUnit unit)
            throws IOException {
        
        try {
            lookupManager.handleResponse((NodeResponse)response, time, unit);
        } finally {
            process(1);
        }
    }

    @Override
    protected synchronized void processTimeout(RequestMessage request, 
            long time, TimeUnit unit) throws IOException {
        
        try {
            lookupManager.handleTimeout(request, time, unit);
        } finally {
            process(1);
        }
    }
    
    /**
     * 
     */
    private static class LookupManager {
        
        private static final boolean EXHAUSTIVE = false;
        
        private static final boolean RANDOMIZE = true;
        
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
        
        /**
         * A history of all {@link KUID}s that were added to the 
         * {@link #query} {@link NavigableSet}.
         */
        private final Map<KUID, Integer> history 
            = new HashMap<KUID, Integer>();
        
        private int currentHop = 0;
        
        private int timeouts = 0;
        
        public LookupManager(RouteTable routeTable, KUID key) {
            if (routeTable == null) {
                throw new NullPointerException("routeTable");
            }
            
            if (key == null) {
                throw new NullPointerException("key");
            }
            
            this.routeTable = routeTable;
            this.key = key;
            
            Contact localhost = routeTable.getLocalhost();
            KUID contactId = localhost.getContactId();
            
            XorComparator comparator = new XorComparator(key);
            this.responses = new TreeSet<Contact>(comparator);
            this.closest = new TreeSet<Contact>(comparator);
            this.query = new TreeSet<Contact>(comparator);
            
            history.put(contactId, 0);
            Contact[] contacts = routeTable.select(key);
            
            if (0 < contacts.length) {
                addToResponses(localhost);
                
                for (Contact contact : contacts) {
                    addToQuery(contact, 1);
                }
            }
        }
        
        public void handleResponse(NodeResponse response, 
                long time, TimeUnit unit) {
            
            boolean success = addToResponses(response.getContact());
            if (!success) {
                return;
            }
            
            Contact[] contacts = response.getContacts();
            for (Contact contact : contacts) {
                if (addToQuery(contact, currentHop+1)) {
                    routeTable.add(contact);
                }
            }
        }
        
        public void handleTimeout(RequestMessage request, 
                long time, TimeUnit unit) {
            timeouts++;
        }
        
        public Contact[] getContacts() {
            return responses.toArray(new Contact[0]);
        }
        
        public int getCurrentHop() {
            return currentHop;
        }
        
        public int getTimeouts() {
            return timeouts;
        }
        
        private boolean addToResponses(Contact contact) {
            if (responses.add(contact)) {
                closest.add(contact);
                
                if (closest.size() > routeTable.getK()) {
                    closest.pollLast();
                }
                
                KUID contactId = contact.getContactId();
                currentHop = history.get(contactId);
                return true;
            }
            
            return false;
        }
        
        private boolean addToQuery(Contact contact, int hop) {
            KUID contactId = contact.getContactId();
            if (!history.containsKey(contactId)) { 
                history.put(contactId, hop);
                query.add(contact);
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
                if (closest.size() < routeTable.getK() 
                        || isCloserThanClosest(contact) 
                        || EXHAUSTIVE) {
                    return true;
                }
            }
            
            return false;
        }
        
        public Contact next() {
            Contact contact = null;
            
            if (RANDOMIZE) {
                
                // TODO: There is a much better way to do this!
                if (!query.isEmpty()) {
                    List<Contact> contacts = new ArrayList<Contact>();
                    for (Contact c : query) {
                        contacts.add(c);
                        if (contacts.size() >= routeTable.getK()) {
                            break;
                        }
                    }
                    
                    contact = contacts.get((int)(Math.random() * contacts.size()));
                    query.remove(contact);
                }
                
            } else {
                contact = query.pollFirst();
            }
            
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
}
