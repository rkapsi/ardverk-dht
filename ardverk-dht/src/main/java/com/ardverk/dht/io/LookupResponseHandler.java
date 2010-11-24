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
import org.ardverk.lang.Arguments;
import org.ardverk.lang.NullArgumentException;
import org.slf4j.Logger;

import com.ardverk.dht.KUID;
import com.ardverk.dht.config.LookupConfig;
import com.ardverk.dht.entity.LookupEntity;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.RouteTable;
import com.ardverk.dht.settings.KademliaSettings;
import com.ardverk.dht.utils.SchedulingUtils;
import com.ardverk.logging.LoggerUtils;

public abstract class LookupResponseHandler<T extends LookupEntity> 
        extends AbstractResponseHandler<T> {
    
    private static final Logger LOG 
        = LoggerUtils.getLogger(LookupResponseHandler.class);
    
    protected final LookupConfig config;
    
    private final LookupManager lookupManager;
    
    private final ProcessCounter lookupCounter;
    
    private long startTime = -1L;
    
    private ScheduledFuture<?> boostFuture;
    
    public LookupResponseHandler(MessageDispatcher messageDispatcher, 
            RouteTable routeTable, KUID lookupId, LookupConfig config) {
        super(messageDispatcher);
        
        this.config = Arguments.notNull(config, "config");
        lookupManager = new LookupManager(routeTable, lookupId);
        lookupCounter = new ProcessCounter(config.getAlpha());
    }

    @Override
    protected synchronized void go(AsyncFuture<T> future) throws IOException {
        
        long boostFrequency = config.getBoostFrequencyInMillis();
        
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
                    task, boostFrequency, boostFrequency, 
                    TimeUnit.MILLISECONDS);
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
            long boostTimeout = config.getBoostTimeoutInMillis();
            if (getLastResponseTime(TimeUnit.MILLISECONDS) >= boostTimeout) {
                try {
                    Contact contact = lookupManager.next();
                    
                    lookup(contact);
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
                
                Contact contact = lookupManager.next();
                
                lookup(contact);
                lookupCounter.increment();
            }
        } finally {
            postProcess();
        }
    }
    
    private void lookup(Contact dst) throws IOException {
        long defaultTimeout = config.getFooTimeoutInMillis();
        long adaptiveTimeout = config.getAdaptiveTimeout(
                dst, defaultTimeout, TimeUnit.MILLISECONDS);
        lookup(dst, lookupManager.key, adaptiveTimeout, TimeUnit.MILLISECONDS);
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
    protected abstract void lookup(Contact dst, KUID lookupId, 
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
    protected synchronized void processContacts(Contact src, 
            Contact[] contacts, long time, TimeUnit unit) throws IOException {
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
        
        Contact[] contacts = lookupManager.getContacts();
        int hop = lookupManager.getCurrentHop();
        long time = System.currentTimeMillis() - startTime;
        
        return new State(contacts, hop, time, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 
     */
    private class LookupManager {
        
        private final boolean exhaustive = config.isExhaustive();
        
        private final boolean randomize = config.isRandomize();
        
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
                throw new NullArgumentException("routeTable");
            }
            
            if (key == null) {
                throw new NullArgumentException("key");
            }
            
            this.routeTable = routeTable;
            this.key = key;
            
            Contact localhost = routeTable.getLocalhost();
            KUID contactId = localhost.getId();
            
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
        
        public void handleResponse(Contact src, Contact[] contacts, 
                long time, TimeUnit unit) {
            
            boolean success = addToResponses(src);
            if (!success) {
                return;
            }
            
            for (Contact contact : contacts) {
                if (addToQuery(contact, currentHop+1)) {
                    routeTable.add(contact);
                }
            }
        }
        
        public void handleTimeout(long time, TimeUnit unit) {
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
                
                KUID contactId = contact.getId();
                currentHop = history.get(contactId);
                return true;
            }
            
            return false;
        }
        
        private boolean addToQuery(Contact contact, int hop) {
            KUID contactId = contact.getId();
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
                KUID contactId = contact.getId();
                KUID otherId = other.getId();
                return otherId.isCloserTo(key, contactId);
            }
            
            return true;
        }
        
        public boolean hasNext() {
            return hasNext(false);
        }
        
        public boolean hasNext(boolean force) {
            if (!query.isEmpty()) {
                
                Contact contact = query.first();
                if (force || exhaustive
                        || closest.size() < KademliaSettings.K 
                        || isCloserThanClosest(contact)) {
                    return true;
                }
            }
            
            return false;
        }
        
        public Contact next() {
            Contact contact = null;
            
            if (randomize) {
                
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
    private static class XorComparator implements Comparator<Contact>, Serializable {
        
        private static final long serialVersionUID = -7543333434594933816L;
        
        private final KUID key;
        
        public XorComparator(KUID key) {
            if (key == null) {
                throw new NullArgumentException("key");
            }
            
            this.key = key;
        }
        
        @Override
        public int compare(Contact o1, Contact o2) {
            return o1.getId().xor(key).compareTo(o2.getId().xor(key));
        }
    }
    
    public static class State {
        
        private final Contact[] contacts;
        
        private final int hop;
        
        private final long time;
        
        private final TimeUnit unit;
        
        private State(Contact[] contacts, int hop, long time, TimeUnit unit) {
            if (contacts == null) {
                throw new NullArgumentException("contacts");
            }
            
            if (unit == null) {
                throw new NullArgumentException("unit");
            }
            
            this.contacts = contacts;
            this.hop = hop;
            this.time = time;
            this.unit = unit;
        }

        public Contact[] getContacts() {
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
