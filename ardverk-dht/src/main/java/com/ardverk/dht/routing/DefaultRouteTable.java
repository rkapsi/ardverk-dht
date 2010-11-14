package com.ardverk.dht.routing;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.ardverk.collection.Cursor;
import org.ardverk.collection.Cursor.Decision;
import org.ardverk.collection.FixedSizeHashMap;
import org.ardverk.collection.KeyAnalyzer;
import org.ardverk.collection.PatriciaTrie;
import org.ardverk.collection.Trie;
import org.ardverk.lang.NullArgumentException;
import org.ardverk.net.NetworkCounter;
import org.ardverk.net.NetworkMask;
import org.slf4j.Logger;

import com.ardverk.dht.KUID;
import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.logging.LoggerUtils;

public class DefaultRouteTable extends AbstractRouteTable {
    
    private static final long serialVersionUID = 2942655317183321858L;
    
    private static final Logger LOG 
        = LoggerUtils.getLogger(DefaultRouteTable.class);
    
    private static enum ContactType {
        ACTIVE,
        CACHED;
    }
    
    private static final long DEFAULT_TIMEOUT = 30L * 1000L;
    
    private final long timeout = DEFAULT_TIMEOUT;
    
    private final TimeUnit unit = TimeUnit.MILLISECONDS;
    
    private final Contact localhost;
    
    private final KeyAnalyzer<KUID> keyAnalyzer;
    
    private final Trie<KUID, Bucket> buckets;
    
    private int consecutiveErrors = 0;
    
    public DefaultRouteTable(int k, Contact localhost) {
        super(k);
        
        if (localhost == null) {
            throw new NullArgumentException("localhost");
        }
        
        KUID contactId = localhost.getContactId();
        this.keyAnalyzer = KUID.createKeyAnalyzer(contactId);
        
        this.localhost = localhost;
        this.buckets = new PatriciaTrie<KUID, Bucket>(keyAnalyzer);
        
        init();
    }
    
    private synchronized void init() {
        consecutiveErrors = 0;
        
        KUID contactId = localhost.getContactId();
        KUID bucketId = contactId.min();
        
        Bucket bucket = new Bucket(bucketId, 0, getK(), getMaxCacheSize());
        buckets.put(bucketId, bucket);
        
        add0(localhost);
    }
    
    @Override
    public Contact getLocalhost() {
        return localhost;
    }
    
    private boolean isLocalhost(ContactEntity entity) {
        return isLocalhost(entity.getContact());
    }
    
    private boolean isLocalhost(Contact contact) {
        return localhost.getContactId().equals(contact.getContactId());
    }
    
    public long getTimeout(TimeUnit unit) {
        return unit.convert(timeout, this.unit);
    }
    
    public long getTimeoutInMillis() {
        return getTimeout(TimeUnit.MILLISECONDS);
    }
    
    private void checkKeyLength(Contact other) throws IllegalArgumentException {
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
    public synchronized void add(Contact contact) {
        if (contact == null) {
            throw new NullArgumentException("contact");
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
    
    private synchronized void add0(Contact contact) {
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
            ContactEntity entity, Contact contact) {
        
        assert (!entity.same(localhost) 
                && !contact.equals(localhost));
        
        // 
        if (entity.isAlive() && !contact.isSolicited()) {
            return;
        }
        
        if (entity.isSameRemoteAddress(contact)) {
            Contact[] merged = entity.update(contact);
            fireContactChanged(bucket, merged[0], merged[1]);
        } else {
            // Spoof Check
        }
    }
    
    private static final int MAX_PER_BUCKET = Integer.MAX_VALUE;
    
    private synchronized boolean isOkayToAdd(Bucket bucket, Contact contact) {
        SocketAddress address = contact.getRemoteAddress();
        return bucket.getActiveCount(address) < MAX_PER_BUCKET;
    }
    
    private synchronized void addActive(Bucket bucket, Contact contact) {
        ContactEntity entity = new ContactEntity(contact);
        boolean success = bucket.addActive(entity);
        
        if (success) {
            fireContactAdded(bucket, contact);
        }
    }
    
    private synchronized ContactEntity addCache(Bucket bucket, Contact contact) {
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
    
    private synchronized void replaceCache(Bucket bucket, Contact contact) {
        ContactEntity lrs = bucket.getLeastRecentlySeenActiveContact();
        
        if (contact.isActive() && isOkayToAdd(bucket, contact)) {
            
            if (!isLocalhost(lrs) /* TODO OTHER CONDITION! */) {
                
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
            ping(lrs);
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
    public synchronized Contact get(KUID contactId) {
        if (contactId == null) {
            throw new NullArgumentException("contactId");
        }
        
        Bucket bucket = buckets.selectValue(contactId);
        ContactEntity entity = bucket.get(contactId);
        return entity != null ? entity.getContact() : null;
    }

    @Override
    public synchronized Contact[] select(KUID contactId, int count) {
        List<Contact> dst = new ArrayList<Contact>(count);
        selectR(contactId, dst, count);
        return dst.toArray(new Contact[0]);
    }
    
    private synchronized void selectR(final KUID contactId, 
            final Collection<Contact> dst, final int count) {
        
        if (contactId == null) {
            throw new NullArgumentException("contactId");
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
    
    @Override
    public synchronized KUID[] select(final long timeout, final TimeUnit unit) {
        final List<KUID> bucketIds = new ArrayList<KUID>();
        final KUID localhostId = localhost.getContactId();
        
        buckets.select(localhostId, new Cursor<KUID, Bucket>() {
            @Override
            public Decision select(Entry<? extends KUID, ? extends Bucket> entry) {
                Bucket bucket = entry.getValue();
                
                if (!bucket.contains(localhostId) 
                        && bucket.isTimeout(timeout, unit)) {
                    // Select a random ID with this prefix
                    KUID randomId = KUID.createWithPrefix(
                            bucket.getBucketId(), bucket.getDepth());
                    
                    bucketIds.add(randomId);
                    bucket.touch();
                }
                
                return Decision.CONTINUE;
            }
        });
        
        return bucketIds.toArray(new KUID[0]);
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
    
    private synchronized ArdverkFuture<PingEntity> ping(ContactEntity entity) {
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
        
        Contact contact = entity.getContact();
        long timeout = contact.getAdaptiveTimeout(this.timeout, unit);
        return ping(contact, timeout, unit);
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
    
    public synchronized ContactEntity[] getActiveContacts() {
        return getContacts(ContactType.ACTIVE);
    }

    public synchronized ContactEntity[] getCachedContacts() {
        return getContacts(ContactType.CACHED);
    }

    private ContactEntity[] getContacts(ContactType contactType) {
        List<ContactEntity> contacts = new ArrayList<ContactEntity>();
        for (Bucket bucket : buckets.values()) {
            
            ContactEntity[] entitis = bucket.getContacts(contactType);
            
            for (ContactEntity entity : entitis) {
                contacts.add(entity);
            }
        }
        
        return contacts.toArray(new ContactEntity[0]);
    }
    
    public synchronized void rebuild() {
        ContactEntity[] active = getActiveContacts();
        ContactEntity[] cached = getCachedContacts();
        
        clear();
        
        ContactUtils.byHealth(active);
        for (ContactEntity entity : active) {
            if (entity.isDead()) {
                break;
            }
            
            add(entity.getContact());
        }
        
        ContactUtils.byTimeStamp(cached);
        for (ContactEntity entity : cached) {
            add(entity.getContact());
        }
    }
    
    public synchronized void clear() {
        buckets.clear();
        init();
    }
    
    @Override
    public synchronized int size() {
        int size = 0;
        
        for (Bucket bucket : buckets.values()) {
            size += bucket.size();
        }
        
        return size;
    }
    
    public class Bucket {
        
        private final long creationTime = System.currentTimeMillis();
        
        private final KUID bucketId;
        
        private final int depth;
        
        private final Trie<KUID, ContactEntity> active;
        
        private final FixedSizeHashMap<KUID, ContactEntity> cached;
        
        private final NetworkCounter counter 
            = new NetworkCounter(NetworkMask.C);
        
        private long timeStamp = creationTime;
        
        private Bucket(KUID bucketId, int depth, int k, int maxCacheSize) {
            if (bucketId == null) {
                throw new NullArgumentException("bucketId");
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
        
        public long getCreationTime() {
            return creationTime;
        }
        
        public long getTimeStamp() {
            return timeStamp;
        }
        
        public void touch() {
            timeStamp = System.currentTimeMillis();
        }
        
        public boolean isTimeout(long timeout, TimeUnit unit) {
            long timeoutInMillis = unit.toMillis(timeout);
            return (System.currentTimeMillis()-timeStamp) >= timeoutInMillis;
        }
        
        public int size() {
            return active.size();
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
                final Collection<Contact> dst, final int count) {
            
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
        
        public ContactEntity[] getContacts(ContactType contactType) {
            switch (contactType) {
                case ACTIVE:
                    return getActive();
                case CACHED:
                    return getCached();
                default:
                    throw new IllegalArgumentException("contactType=" + contactType);
            }
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
            Contact contact = entity.getContact();
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
            Contact contact = entity.getContact();
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
            
            Contact contact = other.getContact();
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
