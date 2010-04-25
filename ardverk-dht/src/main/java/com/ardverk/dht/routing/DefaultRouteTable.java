package com.ardverk.dht.routing;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.ardverk.collection.Cursor;
import org.ardverk.collection.FixedSizeHashMap;
import org.ardverk.collection.KeyAnalyzer;
import org.ardverk.collection.PatriciaTrie;
import org.ardverk.collection.Trie;
import org.ardverk.collection.Cursor.Decision;
import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncFutureListener;
import org.slf4j.Logger;

import com.ardverk.dht.ContactPinger;
import com.ardverk.dht.KUID;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.routing.Contact2.Type;
import com.ardverk.logging.LoggerUtils;
import com.ardverk.net.NetworkCounter;
import com.ardverk.net.NetworkMask;
import com.ardverk.utils.NetworkUtils;

public class DefaultRouteTable extends AbstractRouteTable {
    
    private static final long serialVersionUID = 2942655317183321858L;
    
    private static final Logger LOG 
        = LoggerUtils.getLogger(DefaultRouteTable.class);
    
    private final Contact2 localhost;
    
    private final KeyAnalyzer<KUID> keyAnalyzer;
    
    private final Trie<KUID, Bucket> buckets;
    
    private int consecutiveErrors = 0;
    
    public DefaultRouteTable(ContactPinger pinger, int k, Contact2 localhost) {
        super(pinger, k);
        
        if (localhost == null) {
            throw new NullPointerException("localhost");
        }
        
        KUID contactId = localhost.getContactId();
        this.keyAnalyzer = KUID.createKeyAnalyzer(contactId);
        
        this.localhost = localhost;
        this.buckets = new PatriciaTrie<KUID, Bucket>(keyAnalyzer);
        
        init();
    }
    
    @Override
    public Contact2 getLocalhost() {
        return localhost;
    }
    
    private synchronized void init() {
        consecutiveErrors = 0;
        
        KUID contactId = localhost.getContactId();
        KUID bucketId = contactId.min();
        
        Bucket bucket = new Bucket(bucketId, 0, getK(), getMaxCacheSize());
        buckets.put(bucketId, bucket);
        
        add0(localhost);
    }
    
    private boolean isLocalhost(ContactEntity entity) {
        return isLocalhost(entity.getContact());
    }
    
    private boolean isLocalhost(Contact2 contact) {
        return localhost.getContactId().equals(contact.getContactId());
    }
    
    private void checkKeyLength(Contact2 other) throws IllegalArgumentException {
        KUID contactId = localhost.getContactId();
        KUID otherId = other.getContactId();
        if (contactId.lengthInBits() 
                != otherId.lengthInBits()) {
            throw new IllegalArgumentException(
                    "Bits: " + contactId.lengthInBits() 
                    + " vs. " + contactId.lengthInBits());
        }
    }
    
    @Override
    public synchronized void add(Contact2 contact) {
        if (contact == null) {
            throw new NullPointerException("contact");
        }
        
        // Make sure the KUIDs of all Contacts have the
        // same length in bits as the localhost Contact!
        checkKeyLength(contact);
        
        // Nobody and nothing can add a Contact that has 
        // the exact same KUID as the localhost Contact!
        if (isLocalhost(contact)) {
            return;
        }
        
        // Reset the consecutive errors counter every time
        // we receive a "message" from an actual Contact.
        if (contact.isActive()) {
            consecutiveErrors = 0;
        }
        
        add0(contact);
    }
    
    private synchronized void add0(Contact2 contact) {
        KUID contactId = contact.getContactId();
        Bucket bucket = buckets.selectValue(contactId);
        ContactEntity entity = bucket.get(contactId);
        
        if (entity != null) {
            updateContact(bucket, entity, contact);
        } else if (!bucket.isActiveFull()) {
            if (isOkayToAdd(bucket, contact)) {
                addActive(bucket, contact);
            } else if (!canSplit(bucket)) {
                addCache(bucket, contact);
            }
        } else if (split(bucket)) {
            add0(contact);
        } else {
            replaceCache(bucket, contact);
        }
    }
    
    private synchronized void updateContact(Bucket bucket, 
            ContactEntity entity, Contact2 contact) {
        
        assert (!entity.same(localhost) 
                && !contact.equals(localhost));
        
        // 
        if (entity.isAlive() && !contact.isSolicited()) {
            return;
        }
        
        if (entity.isSameRemoteAddress(contact)) {
            Contact2[] merged = entity.update(contact);
            fireContactChanged(bucket, merged[0], merged[1]);
        } else {
            // Spoof Check
        }
    }
    
    private static final int MAX_PER_BUCKET = Integer.MAX_VALUE;
    
    private synchronized boolean isOkayToAdd(Bucket bucket, Contact2 contact) {
        SocketAddress address = contact.getRemoteAddress();
        return bucket.getActiveCount(address) < MAX_PER_BUCKET;
    }
    
    private synchronized void addActive(Bucket bucket, Contact2 contact) {
        ContactEntity entity = new ContactEntity(contact);
        boolean success = bucket.addActive(entity);
        
        if (success) {
            fireContactAdded(bucket, contact);
        }
    }
    
    private synchronized ContactEntity addCache(Bucket bucket, Contact2 contact) {
        ContactEntity entity = new ContactEntity(contact);
        ContactEntity other = bucket.addCache(entity);
        
        if (other != null) {
            if (entity == other) {
                fireContactAdded(bucket, contact);
            } else {
                fireContactReplaced(bucket, other.getContact(), contact);
            }
        }
        
        return other;
    }
    
    private synchronized void replaceCache(Bucket bucket, Contact2 contact) {
        ContactEntity lrs = bucket.getLeastRecentlySeenActiveContact();
        
        if (contact.isActive() && isOkayToAdd(bucket, contact)) {
            
            if (!isLocalhost(lrs) /* OTHER CONDITION! */) {
                
                bucket.removeActive(lrs);
                bucket.addActive(new ContactEntity(contact));
                
                fireReplaceContact(bucket, lrs.getContact(), contact);
                
                return;
            }
        }
        
        addCache(bucket, contact);
        pingLeastRecentlySeenContact(bucket);
    }
    
    private synchronized void pingLeastRecentlySeenContact(Bucket bucket) {
        ContactEntity lrs = bucket.getLeastRecentlySeenActiveContact();
        if (!isLocalhost(lrs)) {
            ping(lrs, null);
        }
    }
    
    private synchronized boolean split(Bucket bucket) {
        if (canSplit(bucket)) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Splitting Bucket: " + bucket);
            }
            
            Bucket[] split = bucket.split();
            assert (split.length == 2);
            
            Bucket left = split[0];
            Bucket right = split[1];
            
            // The left one replaces the existing Bucket
            Bucket oldLeft = buckets.put(left.getBucketId(), left);
            assert (oldLeft == bucket);
            
            // The right one is new in the RouteTable
            Bucket oldRight = buckets.put(right.getBucketId(), right);
            assert (oldRight == null);
            
            fireSplitBucket(bucket, left, right);
            return true;
        }
        
        return false;
    }
    
    private synchronized boolean canSplit(Bucket bucket) {
        
        // We *split* the Bucket if:
        // 1. Bucket contains the localhost Contact
        // 2. Bucket is smallest subtree
        // 3. Bucket hasn't reached its max depth
        KUID contactId = localhost.getContactId();
        
        if (bucket.contains(contactId)
                || isSmallestSubtree(bucket)
                || !isTooDeep(bucket)) {
            return true;
        }
        return false;
    }
    
    @Override
    public synchronized Contact2 get(KUID contactId) {
        if (contactId == null) {
            throw new NullPointerException("contactId");
        }
        
        Bucket bucket = buckets.selectValue(contactId);
        ContactEntity entity = bucket.get(contactId);
        return entity != null ? entity.getContact() : null;
    }

    @Override
    public synchronized Contact2[] select(KUID contactId, int count) {
        List<Contact2> dst = new ArrayList<Contact2>(count);
        selectR(contactId, dst, count);
        return dst.toArray(new Contact2[0]);
    }
    
    private synchronized void selectR(final KUID contactId, 
            final Collection<Contact2> dst, final int count) {
        
        if (contactId == null) {
            throw new NullPointerException("contactId");
        }
        
        if (dst.size() >= count) {
            return;
        }
       
        buckets.select(contactId, new Cursor<KUID, Bucket>() {
            @Override
            public Decision select(Entry<? extends KUID, ? extends Bucket> entry) {
                Bucket bucket = entry.getValue();
                return bucket.select(contactId, dst, count);
            }
        });
    }
    
    public int getMaxDepth() {
        return Integer.MAX_VALUE;
    }
    
    public int getMaxCacheSize() {
        return 16;
    }
    
    /**
     * Returns true if the given {@link Bucket} has reached its maximum
     * depth in the RoutingTable Tree.
     */
    private synchronized boolean isTooDeep(Bucket bucket) {
        return bucket.getDepth() >= getMaxDepth();
    }
    
    /**
     * Returns true if the given {@link Bucket} is the closest left
     * or right hand sibling of the {@link Bucket} which contains 
     * the localhost {@link Contact}.
     */
    private synchronized boolean isSmallestSubtree(Bucket bucket) {
        KUID contactId = localhost.getContactId();
        KUID bucketId = bucket.getBucketId();
        int prefixLength = contactId.commonPrefix(bucketId);
        
        // The sibling Bucket contains the localhost Contact. 
        // We're looking if the other Bucket is its sibling 
        // (what we call the smallest subtree).
        Bucket sibling = buckets.selectValue(contactId);
        return (sibling.getDepth() - 1) == prefixLength;
    }
    
    private synchronized void ping(ContactEntity entity, 
            AsyncFutureListener<PingEntity> listener) {
        /*AsyncFutureListener<PingResponse> listener 
                = new AsyncFutureListener<PingResponse>() {
            @Override
            public void operationComplete(AsyncFuture<PingResponse> future) {
                if (!future.throwsException()) {
                    try {
                        PingResponse response = future.get();
                        fireCollision(response.getContact());
                    } catch (InterruptedException err) {
                        LOG.error("InterruptedException", err);
                    } catch (ExecutionException err) {
                        LOG.error("ExecutionException", err);
                    }
                }
            }
        };*/
        
        AsyncFuture<PingEntity> future 
            = pinger.ping(entity.getContact());
        
        if (listener != null) {
            future.addAsyncFutureListener(listener);
        }
    }
    
    private static final int MAX_CONSECUTIVE_ERRORS = 100;
    
    @Override
    public synchronized void failure(KUID contactId, SocketAddress address) {
        if (contactId == null) {
            return;
        }
        
        Bucket bucket = buckets.selectValue(contactId);
        ContactEntity entity = bucket.get(contactId);
        
        if (entity == null) {
            return;
        }
        
        // Make sure we're not going kill the entire RouteTable 
        // if the Network goes down!
        if (++consecutiveErrors >= MAX_CONSECUTIVE_ERRORS) {
            return;
        }
        
        boolean dead = entity.error();
        if (dead) {
            
        }
    }
    
    @Override
    public synchronized void rebuild() {
        
    }
    
    private static class ContactEntity {
        
        private static final int MAX_ERRORS = 5;
        
        private final long creationTime = System.currentTimeMillis();
        
        private long timeStamp = creationTime;
        
        private final KUID contactId;
        
        private Contact2 contact;
        
        private int errorCount = 0;
        
        public ContactEntity(Contact2 contact) {
            if (contact == null) {
                throw new NullPointerException("contact");
            }
            
            this.contactId = contact.getContactId();
            this.contact = contact;
        }
        
        public long getCreationTime() {
            return creationTime;
        }
        
        public long getTimeStamp() {
            return timeStamp;
        }
        
        public KUID getContactId() {
            return contactId;
        }
        
        public Contact2 getContact() {
            return contact;
        }
        
        public Contact2[] update(Contact2 contact) {
            if (contact == null) {
                throw new NullPointerException("contact");
            }
            
            if (!contactId.equals(contact.getContactId())) {
                throw new IllegalArgumentException();
            }
            
            Contact2 previous = this.contact;
            
            if (previous != null) {
                if (previous.getType() == Type.SOLICITED
                        && contact.getType() == Type.UNSOLICITED) {
                    throw new IllegalArgumentException();
                }
                
                //contact = new DefaultContact(previous, contact);
                contact = previous.merge(contact);
            }
            
            this.contact = contact;
            this.timeStamp = System.currentTimeMillis();
            
            return new Contact2[] { previous, contact };
        }
        
        public Contact2 replaceContact(Contact2 contact) {
            return update(contact)[0];
        }
        
        public boolean error() {
            ++errorCount;
            return isDead();
        }
        
        public boolean isSolicited() {
            return contact.isSolicited();
        }
        
        public boolean isUnsolicited() {
            return contact.isUnsolicited();
        }
        
        public boolean isDead() {
            return errorCount >= MAX_ERRORS;
        }
        
        public boolean isAlive() {
            return !isDead() && contact.isActive();
        }
        
        public boolean isUnknown() {
            return !isDead() && isUnsolicited();
        }
        
        public boolean isSameRemoteAddress(Contact2 contact) {
            return NetworkUtils.isSameAddress(
                    this.contact.getRemoteAddress(), 
                    contact.getRemoteAddress());
        }
        
        private static final long X = 5L*60L*1000L;
        
        public boolean hasBeenActiveRecently() {
            return (System.currentTimeMillis() - getTimeStamp()) < X;
        }
        
        public boolean same(Contact2 other) {
            if (other == null) {
                throw new NullPointerException("other");
            }
            
            return contact != null && contact.equals(other);
        }
    }
    
    public class Bucket {
        
        private final KUID bucketId;
        
        private final int depth;
        
        private final Trie<KUID, ContactEntity> active;
        
        private final FixedSizeHashMap<KUID, ContactEntity> cached;
        
        private final NetworkCounter counter 
            = new NetworkCounter(NetworkMask.C);
        
        private Bucket(KUID bucketId, int depth, int k, int maxCacheSize) {
            if (bucketId == null) {
                throw new NullPointerException("bucketId");
            }
            
            if (depth < 0) {
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
            
            active = new PatriciaTrie<KUID, ContactEntity>(keyAnalyzer);
            
            cached = new FixedSizeHashMap<KUID, ContactEntity>(
                    maxCacheSize, 1.0f, true, maxCacheSize);
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
                final Collection<Contact2> dst, final int count) {
            
            active.select(contactId, new Cursor<KUID, ContactEntity>() {
                @Override
                public Decision select(Entry<? extends KUID, 
                        ? extends ContactEntity> entry) {
                    
                    ContactEntity entity = entry.getValue();
                    
                    double probability = 1.0d;
                    if (entity.isDead()) {
                        probability = Math.random();
                    }
                    
                    if (probability >= PROBABILITY) {
                        dst.add(entity.getContact());
                    }
                    
                    return (dst.size() < count ? Decision.CONTINUE : Decision.EXIT);
                }
            });
            
            return (dst.size() < count ? Decision.CONTINUE : Decision.EXIT);
        }
        
        public ContactEntity get(KUID contactId) {
            ContactEntity entity = active.get(contactId);
            if (entity == null) {
                entity = cached.get(contactId);
            }
            return entity;
        }
        
        public ContactEntity[] getActive() {
            return active.values().toArray(new ContactEntity[0]);
        }
        
        public ContactEntity[] getCached() {
            return cached.values().toArray(new ContactEntity[0]);
        }
        
        public int getActiveCount(SocketAddress address) {
            return counter.get(address);
        }
        
        public void add(ContactEntity entity) {
            // Remove it from the Cache if it's there
            removeCache(entity);
            
            // Add it to the active RouteTable if possible
            boolean success = addActive(entity);
            
            // Add the Contact back to the Cache if it was not 
            // possible to add it to the active RouteTable
            if (!success) {
                addCache(entity);
            }
        }
        
        private boolean addActive(ContactEntity entity) {
            Contact2 contact = entity.getContact();
            KUID contactId = contact.getContactId();
            
            // Make sure Bucket does not contain the Contact!
            assert (!contains(contactId));
                
            if (hasOrMakeSpace()) {
                active.put(contactId, entity);
                counter.add(contact.getRemoteAddress());
                return true;
            }
            
            return false;
        }
        
        private ContactEntity addCache(ContactEntity entity) {
            Contact2 contact = entity.getContact();
            KUID contactId = contact.getContactId();
            
            // Make sure Bucket does not contain the Contact!
            assert (!contains(contactId));
            
            if (!isCacheFull()) {
                cached.put(contactId, entity);
                return entity;
            }
            
            ContactEntity lrs = getLeastRecentlySeenCachedContact();
            if (lrs.isDead() || (!lrs.hasBeenActiveRecently() && !entity.isDead())) {
                ContactEntity removed = cached.remove(lrs.getContactId());
                assert (lrs == removed);
                
                cached.put(contactId, entity);
                return removed;
            }
            
            return null;
        }
        
        private ContactEntity getLeastRecentlySeenCachedContact() {
            ContactEntity lrs = null;
            for (ContactEntity entity : cached.values()) {
                if (lrs == null || entity.getTimeStamp() < lrs.getTimeStamp()) {
                    lrs = entity;
                }
            }
            return lrs;
        }
        
        private ContactEntity getLeastRecentlySeenActiveContact() {
            ContactEntity lrs = null;
            for (ContactEntity entity : active.values()) {
                if (lrs == null || entity.getTimeStamp() < lrs.getTimeStamp()) {
                    lrs = entity;
                }
            }
            return lrs;
        }
        
        private ContactEntity removeCache(ContactEntity entity) {
            KUID contactId = entity.getContactId();
            return cached.remove(contactId);
        }
        
        private boolean hasOrMakeSpace() {
            if (isActiveFull()) {
                for (ContactEntity current : getActive()) {
                    if (current.isDead()) {
                        removeActive(current);
                        break;
                    }
                }
            }
            
            return !isActiveFull();
        }
        
        private void removeActive(ContactEntity entity) {
            ContactEntity other = active.remove(entity.getContactId());
            assert (entity == other);
            
            Contact2 contact = other.getContact();
            SocketAddress address = contact.getRemoteAddress();
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
            
            for (ContactEntity entity : active.values()) {
                KUID contactId = entity.getContactId();
                if (!contactId.isSet(depth)) {
                    left.add(entity);
                } else {
                    right.add(entity);
                }
            }
            
            for (ContactEntity entity : cached.values()) {
                KUID contactId = entity.getContactId();
                if (!contactId.isSet(depth)) {
                    left.add(entity);
                } else {
                    right.add(entity);
                }
            }
            
            return new Bucket[] { left, right };
        }
        
        public boolean contains(KUID contactId) {
            return active.containsKey(contactId)
                || cached.containsKey(contactId);
        }
    }
    
    /*public static void main(String[] args) {
        TreeMap<KUID, KUID> tree = new TreeMap<KUID, KUID>();
        Trie<KUID, KUID> trie = new PatriciaTrie<KUID, KUID>(KUID.createKeyAnalyzer(160));
        
        Random generator = new Random();
        
        for (int i = 0; i < 5; i++) {
            byte[] data = new byte[20];
            generator.nextBytes(data);
            
            KUID key = new KUID(data);
            
            tree.put(key, key);
            trie.put(key, key);
        }
        
        byte[] data = new byte[20];
        generator.nextBytes(data);
        final KUID lookupKey = new KUID(data);
        
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
    }*/
}
