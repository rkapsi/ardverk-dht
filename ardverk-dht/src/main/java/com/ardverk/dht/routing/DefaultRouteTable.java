package com.ardverk.dht.routing;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.ardverk.collection.Cursor;
import org.ardverk.collection.KeyAnalyzer;
import org.ardverk.collection.PatriciaTrie;
import org.ardverk.collection.Trie;
import org.ardverk.collection.Cursor.Decision;
import org.slf4j.Logger;

import com.ardverk.collection.FixedSizeHashMap;
import com.ardverk.concurrent.AsyncFuture;
import com.ardverk.concurrent.AsyncFutureListener;
import com.ardverk.dht.KUID;
import com.ardverk.dht.KeyFactory;
import com.ardverk.logging.LoggerUtils;
import com.ardverk.net.NetworkConstants;
import com.ardverk.net.NetworkCounter;

public class DefaultRouteTable extends AbstractRouteTable {
    
    private static final long serialVersionUID = 2942655317183321858L;
    
    private static final Logger LOG 
        = LoggerUtils.getLogger(DefaultRouteTable.class);
    
    private final Contact local;
    
    private final KeyAnalyzer<KUID> keyAnalyzer;
    
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
        
        this.keyAnalyzer = KUID.createKeyAnalyzer(lengthInBits);
        
        this.local = local;
        this.buckets = new PatriciaTrie<KUID, Bucket>(keyAnalyzer);
        
        init();
    }
    
    private synchronized void init() {
        KUID bucketId = getKeyFactory().min();
        Bucket bucket = new Bucket(bucketId, 0, getK(), getMaxCacheSize());
        buckets.put(bucketId, bucket);
        
        add(local, State.ALIVE);
    }
    
    @Override
    public synchronized void add(Contact contact, State state) {
        if (contact == null) {
            throw new NullPointerException("contact");
        }
        
        if (state == null) {
            throw new NullPointerException("state");
        }
        
        if (state == State.DEAD) {
            throw new IllegalArgumentException("state=" + state);
        }
        
        KeyFactory keyFactory = getKeyFactory();
        KUID contactId = contact.getContactId();
        if (keyFactory.lengthInBits() != contactId.lengthInBits()) {
            throw new IllegalArgumentException();
        }
        
        innerAdd(contact, state);
    }
    
    private synchronized void innerAdd(Contact contact, State state) {
        KUID contactId = contact.getContactId();
        Bucket bucket = buckets.selectValue(contactId);
        ContactHandle handle = bucket.get(contactId);
        
        if (handle != null) {
            updateContactInBucket(bucket, handle, contact);
        } else if (!bucket.isActiveFull()) {
            
        } else if (split(bucket)) {
            add(contact, state);
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
                return bucket.select(contactId, items, count);
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
        
        boolean dead = handle.errorCount(true);
        if (dead) {
            
        }
    }
    
    @Override
    public synchronized void rebuild() {
        
    }
    
    private static class ContactHandle {
        
        private static final int MAX_ERRORS = 5;
        
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
        
        public boolean errorCount(boolean increment) {
            if (increment) {
                ++errorCount;
            }
            return isDead();
        }
        
        public boolean isDead() {
            return errorCount >= MAX_ERRORS;
        }
    }
    
    public class Bucket {
        
        private final KUID bucketId;
        
        private final int depth;
        
        private final Trie<KUID, ContactHandle> active;
        
        private final FixedSizeHashMap<KUID, ContactHandle> cached;
        
        private final NetworkCounter counter 
            = new NetworkCounter(NetworkConstants.CLASS_C);
        
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
            
            active = new PatriciaTrie<KUID, ContactHandle>(keyAnalyzer);
            cached = new FixedSizeHashMap<KUID, ContactHandle>(maxCacheSize);
        }
        
        public KUID getBucketId() {
            return bucketId;
        }
        
        public int getDepth() {
            return depth;
        }
        
        public int getMaxCacheSize() {
            return cached.getMaxSize();
        }
        
        public int getCacheSize() {
            return cached.size();
        }
        
        public boolean isCacheEmpty() {
            return cached.isEmpty();
        }
        
        public boolean isCacheFull() {
            return cached.isFull();
        }
        
        public boolean isActiveFull() {
            return active.size() >= getK();
        }
        
        public boolean isActiveEmpty() {
            return active.isEmpty();
        }
        
        private static final double PROBABILITY = 0.75d;
        
        public Decision select(KUID contactId, 
                final Collection<Contact> items, final int count) {
            
            active.select(contactId, new Cursor<KUID, ContactHandle>() {
                @Override
                public Decision select(Entry<? extends KUID, 
                        ? extends ContactHandle> entry) {
                    
                    ContactHandle handle = entry.getValue();
                    
                    double probability = 1.0d;
                    if (handle.isDead()) {
                        probability = Math.random();
                    }
                    
                    if (probability >= PROBABILITY) {
                        items.add(handle.getContact());
                    }
                    
                    return (items.size() < count ? Decision.CONTINUE : Decision.EXIT);
                }
            });
            
            return (items.size() < count ? Decision.CONTINUE : Decision.EXIT);
        }
        
        public ContactHandle get(KUID contactId) {
            ContactHandle handle = active.get(contactId);
            if (handle == null) {
                handle = cached.get(contactId);
            }
            return handle;
        }
        
        public ContactHandle[] getActive() {
            return active.values().toArray(new ContactHandle[0]);
        }
        
        public ContactHandle[] getCached() {
            return cached.values().toArray(new ContactHandle[0]);
        }
        
        public void add(ContactHandle handle) {
            
        }
        
        public int getActiveCount(ContactHandle handle) {
            return counter.get(handle.getContact().getRemoteAddress());
        }
        
        private boolean addActive(ContactHandle handle) {
            KUID contactId = handle.getContactId();
            Contact contact = handle.getContact();
            
            boolean contains = active.containsKey(contactId);
            if (contains || makeSpace()) {
                if (contains) {
                    counter.remove(contact.getRemoteAddress());
                }
                
                active.put(contactId, handle);
                counter.add(contact.getRemoteAddress());
                return true;
            }
            
            return false;
        }
        
        private boolean makeSpace() {
            if (isActiveFull()) {
                for (ContactHandle current : getActive()) {
                    if (current.isDead()) {
                        removeActive(current);
                        break;
                    }
                }
            }
            
            return !isActiveFull();
        }
        
        private void removeActive(ContactHandle handle) {
            ContactHandle other = active.remove(handle.getContactId());
            assert (handle == other);
            
            SocketAddress address = other.getContact().getRemoteAddress();
            counter.remove(address);
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
            
            for (ContactHandle handle : cached.values()) {
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
                || cached.containsKey(contactId);
        }
    }
    
    public static void main(String[] args) {
        TreeMap<KUID, KUID> tree = new TreeMap<KUID, KUID>();
        Trie<KUID, KUID> trie = new PatriciaTrie<KUID, KUID>(KUID.createKeyAnalyzer(160));
        
        Random generator = new Random();
        
        for (int i = 0; i < 5; i++) {
            byte[] data = new byte[20];
            generator.nextBytes(data);
            
            KUID key = new KUID(data, KUID.NO_BIT_MASK, 160);
            
            tree.put(key, key);
            trie.put(key, key);
        }
        
        byte[] data = new byte[20];
        generator.nextBytes(data);
        final KUID lookupKey = new KUID(data, KUID.NO_BIT_MASK, 160);
        
        System.out.println("KEY: " + lookupKey);
        System.out.println();
        
        Comparator<KUID> comparator = new Comparator<KUID>() {
            @Override
            public int compare(KUID o1, KUID o2) {
                return o1.xor(lookupKey).compareTo(o2.xor(lookupKey));
            }
        };
        
        KUID[] keys = tree.keySet().toArray(new KUID[0]);
        Arrays.sort(keys, comparator);
        
        for (KUID id : keys) {
            System.out.println("TREE: " + id + ", " + id.xor(lookupKey));
        }
        
        System.out.println();
        
        Cursor<KUID, KUID> cursor = new Cursor<KUID, KUID>() {
            @Override
            public Decision select(Entry<? extends KUID, ? extends KUID> entry) {
                System.out.println("TRIE: " + entry.getKey() + ", " + entry.getKey().xor(lookupKey));
                return Decision.CONTINUE;
            }
        };
        
        trie.select(lookupKey, cursor);
    }
}
