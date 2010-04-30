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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncFuture;
import org.slf4j.Logger;

import com.ardverk.dht.KUID;
import com.ardverk.dht.entity.LookupEntity;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.Contact2;
import com.ardverk.dht.routing.RouteTable;
import com.ardverk.dht.utils.SchedulingUtils;
import com.ardverk.logging.LoggerUtils;

public abstract class LookupResponseHandler<T extends LookupEntity> 
        extends AbstractResponseHandler<T> {
    
    private static final Logger LOG 
        = LoggerUtils.getLogger(LookupResponseHandler.class);
        
    private static final int ALPHA = 4;
    
    private final LookupManager lookupManager;
    
    private final ProcessCounter lookupCounter;
    
    private final long timeout = 3L;
    
    private final TimeUnit unit = TimeUnit.SECONDS;
    
    private long startTime = -1L;
    
    private ScheduledFuture<?> boostFuture;
    
    public LookupResponseHandler(MessageDispatcher messageDispatcher, 
            RouteTable routeTable, KUID key) {
        this(messageDispatcher, routeTable, key, ALPHA);
    }
    
    public LookupResponseHandler(MessageDispatcher messageDispatcher, 
            RouteTable routeTable, KUID key, int alpha) {
        super(messageDispatcher);
        
        lookupManager = new LookupManager(routeTable, key);
        lookupCounter = new ProcessCounter(alpha);
    }

    @Override
    protected synchronized void go(AsyncFuture<T> future) throws IOException {
        
        long boostFrequency = 1000L;
        if (0L < boostFrequency) {
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    try {
                        boost();                    
                    } catch (IOException err) {
                        LOG.error("IOException", err);
                    }
                }
            };
            
            boostFuture = SchedulingUtils.scheduleWithFixedDelay(
                    task, boostFrequency, boostFrequency, TimeUnit.MILLISECONDS);
        }
        
        process(0);
    }
    
    @Override
    protected synchronized void done() {
        if (boostFuture != null) {
            boostFuture.cancel(true);
        }
    }
    
    /**
     * 
     */
    private synchronized void boost() throws IOException {
        if (lookupManager.hasNext(true)) {
            long boostTimeout = 1000L;
            if (getLastResponseTime(TimeUnit.MILLISECONDS) >= boostTimeout) {
                try {
                    Contact2 contact = lookupManager.next();
                    lookup(contact, lookupManager.key, timeout, unit);
                    lookupCounter.increment(true);
                } finally {
                    postProcess();
                }
            }
        }
    }
    
    /**
     * 
     */
    private synchronized void process(int decrement) throws IOException {
        try {
            preProcess(decrement);
            while (lookupCounter.hasNext()) {
                if (!lookupManager.hasNext()) {
                    break;
                }
                
                Contact2 contact = lookupManager.next();
                lookup(contact, lookupManager.key, timeout, unit);
                
                lookupCounter.increment();
            }
        } finally {
            postProcess();
        }
    }
    
    /**
     * 
     */
    private synchronized void preProcess(int decrement) {
        if (startTime == -1L) {
            startTime = System.currentTimeMillis();
        }
        
        while (0 < decrement--) {
            lookupCounter.decrement();
        }
    }
    
    /**
     * 
     */
    private synchronized void postProcess() {
        int count = lookupCounter.getProcesses();
        if (count == 0) {
            State state = getState();
            complete(state);
        }
    }
    
    /**
     * 
     */
    protected abstract void lookup(Contact2 dst, KUID key, 
            long timeout, TimeUnit unit) throws IOException;
    
    /**
     * 
     */
    protected abstract void complete(State state);
    
    
    @Override
    protected final synchronized void processResponse(RequestEntity entity,
            ResponseMessage response, long time, TimeUnit unit)
            throws IOException {
        
        try {
            processResponse0(entity, response, time, unit);
        } finally {
            process(1);
        }
    }
    
    /**
     * 
     */
    protected abstract void processResponse0(RequestEntity entity,
            ResponseMessage response, long time, TimeUnit unit) throws IOException;

    /**
     * 
     */
    protected synchronized void processContacts(Contact2 src, 
            Contact2[] contacts, long time, TimeUnit unit) throws IOException {
        lookupManager.handleResponse(src, contacts, time, unit);
    }

    @Override
    protected final synchronized void processTimeout(RequestEntity entity, 
            long time, TimeUnit unit) throws IOException {
        
        try {
            processTimeout0(entity, time, unit);
        } finally {
            process(1);
        }
    }
    
    protected synchronized void processTimeout0(RequestEntity entity, 
            long time, TimeUnit unit) throws IOException {
        lookupManager.handleTimeout(time, unit);
    }
    
    protected synchronized State getState() {
        if (startTime == -1L) {
            throw new IllegalStateException("startTime=" + startTime);
        }
        
        Contact2[] contacts = lookupManager.getContacts();
        int hop = lookupManager.getCurrentHop();
        long time = System.currentTimeMillis() - startTime;
        
        return new State(contacts, hop, time, TimeUnit.MILLISECONDS);
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
        private final NavigableSet<Contact2> responses;
        
        /**
         * A {@link Set} of the k-closest responses
         */
        private final NavigableSet<Contact2> closest;
        
        /**
         * A {@link Set} of {@link Contact}s to query
         */
        private final NavigableSet<Contact2> query;
        
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
            
            Contact2 localhost = routeTable.getLocalhost();
            KUID contactId = localhost.getContactId();
            
            XorComparator comparator = new XorComparator(key);
            this.responses = new TreeSet<Contact2>(comparator);
            this.closest = new TreeSet<Contact2>(comparator);
            this.query = new TreeSet<Contact2>(comparator);
            
            history.put(contactId, 0);
            Contact2[] contacts = routeTable.select(key);
            
            if (0 < contacts.length) {
                addToResponses(localhost);
                
                for (Contact2 contact : contacts) {
                    addToQuery(contact, 1);
                }
            }
        }
        
        public void handleResponse(Contact2 src, Contact2[] contacts, 
                long time, TimeUnit unit) {
            
            boolean success = addToResponses(src);
            if (!success) {
                return;
            }
            
            for (Contact2 contact : contacts) {
                if (addToQuery(contact, currentHop+1)) {
                    routeTable.add(contact);
                }
            }
        }
        
        public void handleTimeout(long time, TimeUnit unit) {
            timeouts++;
        }
        
        public Contact2[] getContacts() {
            return responses.toArray(new Contact2[0]);
        }
        
        public int getCurrentHop() {
            return currentHop;
        }
        
        public int getTimeouts() {
            return timeouts;
        }
        
        private boolean addToResponses(Contact2 contact) {
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
        
        private boolean addToQuery(Contact2 contact, int hop) {
            KUID contactId = contact.getContactId();
            if (!history.containsKey(contactId)) { 
                history.put(contactId, hop);
                query.add(contact);
                return true;
            }
            
            return false;
        }
        
        private boolean isCloserThanClosest(Contact2 other) {
            if (!closest.isEmpty()) {
                Contact2 contact = closest.last();
                KUID contactId = contact.getContactId();
                KUID otherId = other.getContactId();
                return otherId.isCloserTo(key, contactId);
            }
            
            return true;
        }
        
        public boolean hasNext() {
            return hasNext(false);
        }
        
        public boolean hasNext(boolean force) {
            if (!query.isEmpty()) {
                Contact2 contact = query.first();
                if (force || closest.size() < routeTable.getK() 
                        || isCloserThanClosest(contact) 
                        || EXHAUSTIVE) {
                    return true;
                }
            }
            
            return false;
        }
        
        public Contact2 next() {
            Contact2 contact = null;
            
            if (RANDOMIZE) {
                
                // TODO: There is a much better way to do this!
                if (!query.isEmpty()) {
                    List<Contact2> contacts = new ArrayList<Contact2>();
                    for (Contact2 c : query) {
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
    private static class XorComparator implements Comparator<Contact2>, Serializable {
        
        private static final long serialVersionUID = -7543333434594933816L;
        
        private final KUID key;
        
        public XorComparator(KUID key) {
            if (key == null) {
                throw new NullPointerException("key");
            }
            
            this.key = key;
        }
        
        @Override
        public int compare(Contact2 o1, Contact2 o2) {
            return o1.getContactId().xor(key).compareTo(o2.getContactId().xor(key));
        }
    }
    
    public static class State {
        
        private final Contact2[] contacts;
        
        private final int hop;
        
        private final long time;
        
        private final TimeUnit unit;
        
        private State(Contact2[] contacts, int hop, long time, TimeUnit unit) {
            if (contacts == null) {
                throw new NullPointerException("contacts");
            }
            
            if (unit == null) {
                throw new NullPointerException("unit");
            }
            
            this.contacts = contacts;
            this.hop = hop;
            this.time = time;
            this.unit = unit;
        }

        public Contact2[] getContacts() {
            return contacts;
        }
        
        public int getHop() {
            return hop;
        }

        public long getTime(TimeUnit unit) {
            return unit.convert(time, this.unit);
        }
        
        public long getTimeInMillis() {
            return getTime(TimeUnit.MILLISECONDS);
        }
    }
}
