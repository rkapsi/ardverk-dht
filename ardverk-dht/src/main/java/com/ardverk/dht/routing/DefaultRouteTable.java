package com.ardverk.dht.routing;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.ardverk.collection.Cursor;
import org.ardverk.collection.KeyAnalyzer;
import org.ardverk.collection.SortedPatriciaTrie;
import org.ardverk.collection.Trie;
import org.slf4j.Logger;

import com.ardverk.collection.FixedSizeHashMap;
import com.ardverk.concurrent.AsyncFuture;
import com.ardverk.concurrent.AsyncFutureListener;
import com.ardverk.dht.KUID;
import com.ardverk.dht.KeyFactory;
import com.ardverk.dht.routing.Contact.State;
import com.ardverk.logging.LoggerUtils;

public class DefaultRouteTable extends AbstractRouteTable {
    
    private static final long serialVersionUID = 2942655317183321858L;
    
    private static final Logger LOG 
        = LoggerUtils.getLogger(DefaultRouteTable.class);
    
    private final Contact local;
    
    private final Trie<KUID, Bucket> buckets;
    
    public DefaultRouteTable(ContactFactory contactFactory, 
            Contact local, int k) {
        super(contactFactory, k);
        
        if (contactFactory == null) {
            throw new NullPointerException("contactFactory");
        }
        
        if (local == null) {
            throw new NullPointerException("local");
        }
        
        if (k <= 0) {
            throw new IllegalArgumentException("k=" + k);
        }
        
        KeyFactory keyFactory = getKeyFactory();
        int lengthInBits = keyFactory.lengthInBits();
        
        KeyAnalyzer<KUID> keyAnalyzer 
            = KUID.createKeyAnalyzer(lengthInBits);
        
        this.local = local;
        this.buckets = new SortedPatriciaTrie<KUID, Bucket>(keyAnalyzer);
        
        init();
    }
    
    private synchronized void init() {
        KUID bucketId = getKeyFactory().min();
        Bucket bucket = new Bucket(bucketId, 0, getK(), getMaxCacheSize());
        buckets.put(bucketId, bucket);
        
        add(local);
    }
    
    @Override
    public synchronized void add(Contact contact) {
        if (contact == null) {
            throw new NullPointerException("contact");
        }
        
        if (contact.getState() == State.DEAD) {
            throw new IllegalArgumentException("Dead Contact: " + contact);
        }
        
        innerAdd(contact);
    }
    
    private synchronized void innerAdd(Contact contact) {
        KUID contactId = contact.getContactId();
        Bucket bucket = buckets.selectValue(contactId);
        ContactHandle handle = bucket.get(contactId);
        
        if (handle != null) {
            updateContactInBucket(bucket, handle, contact);
        } else if (!bucket.isActiveFull()) {
            
        } else if (split(bucket)) {
            add(contact);
        } else {
            
        }
    }
    
    private synchronized void updateContactInBucket(Bucket bucket, 
            ContactHandle existing, Contact contact) {
        
    }
    
    @Override
    public Contact[] select(KUID contactId, int count) {
        List<Contact> items = new ArrayList<Contact>(count);
        selectR(contactId, items, count);
        return items.toArray(new Contact[0]);
    }
    
    private synchronized void selectR(final KUID contactId, 
            final Collection<Contact> items, final int count) {
        
        if (contactId == null) {
            throw new NullPointerException("contactId");
        }
        
        if (items.size() >= count) {
            return;
        }
       
        buckets.select(contactId, new Cursor<KUID, Bucket>() {
            @Override
            public Decision select(Entry<? extends KUID, ? extends Bucket> entry) {
                Bucket bucket = entry.getValue();
                
                for (ContactHandle handle : bucket.getActive()) {
                    if (items.size() >= count) {
                        return Decision.EXIT;
                    }
                    
                    items.add(handle.getContact());
                }
                
                return Decision.CONTINUE;
            }
        });
    }
    
    private synchronized boolean split(Bucket bucket) {
        if (canSplitBucket(bucket)) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Splitting Bucket: " + bucket);
            }
            
            Bucket[] buckets = bucket.split();
            assert (buckets.length == 2);
            
            Bucket left = buckets[0];
            Bucket right = buckets[1];
            
            // The left one replaces the existing Bucket
            Bucket oldLeft = this.buckets.put(left.getBucketId(), left);
            assert (oldLeft == bucket);
            
            // The right one is new in the RouteTable
            Bucket oldRight = this.buckets.put(right.getBucketId(), right);
            assert (oldRight == null);
            
            fireSplitBucket(bucket, left, right);
            return true;
        }
        
        return false;
    }
    
    public int getMaxDepth() {
        return Integer.MAX_VALUE;
    }
    
    public int getMaxCacheSize() {
        return 16;
    }
    
    private synchronized boolean isTooDeep(Bucket bucket) {
        return bucket.getDepth() >= getMaxDepth();
    }
    
    private synchronized boolean isSmallestSubtree(Bucket bucket) {
        return false;
    }
    
    private synchronized boolean canSplitBucket(Bucket bucket) {
        
        // We *split* the Bucket if:
        // 1. Bucket contains the Local Contact
        // 2. Bucket is smallest subtree
        // 3. Bucket hasn't reached its max depth
        KUID contactId = local.getContactId();
        
        if (bucket.contains(contactId)
                || isSmallestSubtree(bucket)
                || !isTooDeep(bucket)) {
            return true;
        }
        return false;
    }
    
    private synchronized boolean ping(Contact contact) {
        if (pinger == null) {
            return false;
        }
        
        AsyncFutureListener<?> listener = new AsyncFutureListener<?>() {
            @Override
            public void operationComplete(AsyncFuture<?> future) {
            }
        };
        
        return pinger.ping(contact, listener);
    }
    
    private static final int MAX_ERRORS = 5;
    
    @Override
    public synchronized void failure(KUID contactId, SocketAddress address) {
        if (contactId == null) {
            return;
        }
        
        Bucket bucket = buckets.selectValue(contactId);
        ContactHandle handle = bucket.get(contactId);
        
        if (handle == null) {
            return;
        }
        
        int count = handle.errorCount(true);
        if (count >= MAX_ERRORS) {
            handle.dead();
        }
    }
    
    @Override
    public synchronized void rebuild() {
        
    }
    
    private static class ContactHandle {
        
        private final KUID contactId;
        
        private Contact contact;
        
        private int errorCount = 0;
        
        public ContactHandle(KUID contactId) {
            if (contactId == null) {
                throw new NullPointerException("contactId");
            }
            
            this.contactId = contactId;
        }
        
        public KUID getContactId() {
            return contactId;
        }
        
        public Contact getContact() {
            return contact;
        }
        
        public Contact setContact(Contact contact) {
            if (contact == null) {
                throw new NullPointerException("contact");
            }
            
            if (!contactId.equals(contact.getContactId())) {
                throw new IllegalArgumentException();
            }
            
            Contact previous = this.contact;
            this.contact = contact;
            return previous;
        }
        
        public int errorCount(boolean increment) {
            if (increment) {
                ++errorCount;
            }
            return errorCount;
        }
        
        public void dead() {
            contact = contact.changeState(State.DEAD);
        }
    }
    
    public static class Bucket {
        
        private final KUID bucketId;
        
        private final int depth;
        
        private final FixedSizeHashMap<KUID, ContactHandle> active;
        
        private final FixedSizeHashMap<KUID, ContactHandle> cache;
        
        private Bucket(KUID bucketId, int depth, int k, int maxCacheSize) {
            if (bucketId == null) {
                throw new NullPointerException("bucketId");
            }
            
            if (depth <= 0) {
                throw new IllegalArgumentException("depth=" + depth);
            }
            
            if (k <= 0) {
                throw new IllegalArgumentException("k=" + k);
            }
            
            if (maxCacheSize < 0) {
                throw new IllegalArgumentException(
                        "maxCacheSize=" + maxCacheSize);
            }
            
            this.bucketId = bucketId;
            this.depth = depth;
            
            active = new FixedSizeHashMap<KUID, ContactHandle>(k, k);
            cache = new FixedSizeHashMap<KUID, ContactHandle>(maxCacheSize);
        }
        
        public KUID getBucketId() {
            return bucketId;
        }
        
        public int getDepth() {
            return depth;
        }
        
        public int getK() {
            return active.getMaxSize();
        }
        
        public int getMaxCacheSize() {
            return cache.getMaxSize();
        }
        
        public int getCacheSize() {
            return cache.size();
        }
        
        public boolean isCacheEmpty() {
            return cache.isEmpty();
        }
        
        public boolean isCacheFull() {
            return cache.isFull();
        }
        
        public boolean isActiveFull() {
            return active.isFull();
        }
        
        public boolean isActiveEmpty() {
            return active.isEmpty();
        }
        
        public ContactHandle get(KUID contactId) {
            ContactHandle handle = active.get(contactId);
            if (handle == null) {
                handle = cache.get(contactId);
            }
            return handle;
        }
        
        public ContactHandle[] getActive() {
            return active.values().toArray(new ContactHandle[0]);
        }
        
        public ContactHandle[] getCached() {
            return cache.values().toArray(new ContactHandle[0]);
        }
        
        public void add(ContactHandle handle) {
            
        }
        
        public Bucket[] split() {
            KUID bucketId = getBucketId();
            int depth = getDepth();
            int k = getK();
            int maxCacheSize = getMaxCacheSize();
            
            Bucket left = new Bucket(bucketId, 
                    depth+1, k, maxCacheSize);
            
            Bucket right = new Bucket(bucketId.set(depth), 
                    depth+1, k, maxCacheSize);
            
            for (ContactHandle handle : active.values()) {
                KUID contactId = handle.getContactId();
                if (!contactId.isSet(depth)) {
                    left.add(handle);
                } else {
                    right.add(handle);
                }
            }
            
            for (ContactHandle handle : cache.values()) {
                KUID contactId = handle.getContactId();
                if (!contactId.isSet(depth)) {
                    left.add(handle);
                } else {
                    right.add(handle);
                }
            }
            
            return new Bucket[] { left, right };
        }
        
        public boolean contains(KUID contactId) {
            return active.containsKey(contactId)
                || cache.containsKey(contactId);
        }
    }
}
