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
import org.ardverk.lang.Arguments;
import org.ardverk.lang.NullArgumentException;
import org.ardverk.net.NetworkCounter;
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
    
    private final RouteTableSettings settings;
    
    private final Contact localhost;
    
    private final KeyAnalyzer<KUID> keyAnalyzer;
    
    private final Trie<KUID, DefaultBucket> buckets;
    
    private int consecutiveErrors = 0;
    
    public DefaultRouteTable(Contact localhost) {
        this(new RouteTableSettings(), localhost);
    }
    
    public DefaultRouteTable(RouteTableSettings settings, Contact localhost) {
        this.settings = Arguments.notNull(settings, "settings");
        
        KUID contactId = localhost.getContactId();
        this.keyAnalyzer = KUID.createKeyAnalyzer(contactId);
        
        this.localhost = localhost;
        this.buckets = new PatriciaTrie<KUID, DefaultBucket>(keyAnalyzer);
        
        init();
    }
    
    private synchronized void init() {
        consecutiveErrors = 0;
        
        KUID contactId = localhost.getContactId();
        KUID bucketId = contactId.min();
        
        DefaultBucket bucket = new DefaultBucket(bucketId, 0);
        buckets.put(bucketId, bucket);
        
        add0(localhost);
    }
    
    /**
     * Returns the {@link DefaultRouteTable}'s {@link RouteTableSettings}.
     */
    public RouteTableSettings getSettings() {
        return settings;
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
        DefaultBucket bucket = buckets.selectValue(contactId);
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
    
    private synchronized void updateContact(DefaultBucket bucket, 
            ContactEntity entity, Contact contact) {
        
        assert (!entity.same(localhost) 
                && !contact.equals(localhost));
        
        // 
        if (entity.isAlive() && !contact.isSolicited()) {
            return;
        }
        
        if (entity.isSameRemoteAddress(contact)) {
            ContactEntity.Update merged = entity.update(contact);
            fireContactChanged(bucket, merged.getPrevious(), merged.getMerged());
        } else {
            // Spoof Check
        }
    }
    
    private synchronized boolean isOkayToAdd(DefaultBucket bucket, Contact contact) {
        SocketAddress address = contact.getRemoteAddress();
        return bucket.getActiveCount(address) < settings.getMaxContactsFromSameNetwork();
    }
    
    private synchronized void addActive(DefaultBucket bucket, Contact contact) {
        ContactEntity entity = new ContactEntity(contact);
        boolean success = bucket.addActive(entity);
        
        if (success) {
            fireContactAdded(bucket, contact);
        }
    }
    
    private synchronized ContactEntity addCache(DefaultBucket bucket, Contact contact) {
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
    
    private synchronized void replaceCache(DefaultBucket bucket, Contact contact) {
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
    
    private synchronized void pingLeastRecentlySeenContact(DefaultBucket bucket) {
        ContactEntity lrs = bucket.getLeastRecentlySeenActiveContact();
        if (!isLocalhost(lrs)) {
            ping(lrs);
        }
    }
    
    private synchronized boolean split(DefaultBucket bucket) {
        if (canSplit(bucket)) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Splitting Bucket: " + bucket);
            }
            
            DefaultBucket[] split = bucket.split();
            assert (split.length == 2);
            
            DefaultBucket left = split[0];
            DefaultBucket right = split[1];
            
            // The left one replaces the existing Bucket
            DefaultBucket oldLeft = buckets.put(left.getBucketId(), left);
            assert (oldLeft == bucket);
            
            // The right one is new in the RouteTable
            DefaultBucket oldRight = buckets.put(right.getBucketId(), right);
            assert (oldRight == null);
            
            fireSplitBucket(bucket, left, right);
            return true;
        }
        
        return false;
    }
    
    private synchronized boolean canSplit(DefaultBucket bucket) {
        
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
        
        DefaultBucket bucket = buckets.selectValue(contactId);
        ContactEntity entity = bucket.get(contactId);
        return entity != null ? entity.getContact() : null;
    }
    
    @Override
    public int getK() {
        return settings.getK();
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
       
        buckets.select(contactId, new Cursor<KUID, DefaultBucket>() {
            @Override
            public Decision select(Entry<? extends KUID, ? extends DefaultBucket> entry) {
                DefaultBucket bucket = entry.getValue();
                return bucket.select(contactId, dst, count);
            }
        });
    }
    
    /**
     * Returns true if the given {@link DefaultBucket} has reached its maximum
     * depth in the RoutingTable Tree.
     */
    private synchronized boolean isTooDeep(DefaultBucket bucket) {
        return bucket.getDepth() >= settings.getMaxDepth();
    }
    
    /**
     * Returns true if the given {@link DefaultBucket} is the closest left
     * or right hand sibling of the {@link DefaultBucket} which contains 
     * the localhost {@link Contact}.
     */
    private synchronized boolean isSmallestSubtree(DefaultBucket bucket) {
        KUID contactId = localhost.getContactId();
        KUID bucketId = bucket.getBucketId();
        int prefixLength = contactId.commonPrefix(bucketId);
        
        // The sibling Bucket contains the localhost Contact. 
        // We're looking if the other Bucket is its sibling 
        // (what we call the smallest subtree).
        DefaultBucket sibling = buckets.selectValue(contactId);
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
        
        long defaultTimeout = settings.getTimeoutInMillis();
        long timeout = contact.getAdaptiveTimeout(
                defaultTimeout, TimeUnit.MILLISECONDS);
        return ping(contact, timeout, TimeUnit.MILLISECONDS);
    }
    
    @Override
    public synchronized void failure(KUID contactId, SocketAddress address) {
        // There is nothing we can do if we don't have the KUID.
        // This is possible for PINGs that failed (that means we
        // knew only the SocketAddress of the remote host).
        if (contactId == null) {
            return;
        }
        
        DefaultBucket bucket = buckets.selectValue(contactId);
        ContactEntity entity = bucket.get(contactId);
        
        // Huh? There is no such contact for the given KUID?
        if (entity == null) {
            return;
        }
        
        // Make sure we're not going kill the entire RouteTable 
        // if the Network goes down!
        if (++consecutiveErrors >= settings.getMaxConsecutiveErrors()) {
            return;
        }
        
        boolean dead = entity.error();
        if (dead) {
            // TODO: Replace dead Contact with something from the Cache
        }
    }
    
    /**
     * Returns all ACTIVE {@link ContactEntity}s.
     */
    public synchronized ContactEntity[] getActiveContacts() {
        return getContacts(ContactType.ACTIVE);
    }

    /**
     * Returns all CACHED {@link ContactEntity}s.
     */
    public synchronized ContactEntity[] getCachedContacts() {
        return getContacts(ContactType.CACHED);
    }

    /**
     * Returns {@link ContactEntity}ies of the given {@link ContactType}.
     */
    private ContactEntity[] getContacts(ContactType contactType) {
        List<ContactEntity> contacts = new ArrayList<ContactEntity>();
        for (DefaultBucket bucket : buckets.values()) {
            
            ContactEntity[] entitis = bucket.getContacts(contactType);
            
            for (ContactEntity entity : entitis) {
                contacts.add(entity);
            }
        }
        
        return contacts.toArray(new ContactEntity[0]);
    }
    
    @Override
    public synchronized KUID[] refresh(final long timeout, final TimeUnit unit) {
        final List<KUID> bucketIds = new ArrayList<KUID>();
        final KUID localhostId = localhost.getContactId();
        
        buckets.select(localhostId, new Cursor<KUID, DefaultBucket>() {
            @Override
            public Decision select(Entry<? extends KUID, ? extends DefaultBucket> entry) {
                DefaultBucket bucket = entry.getValue();
                
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
    
    /**
     * Rebuilds the {@link RouteTable} by.
     * 
     * <ol>
     * <li>Making a copy of all ACTIVE and CACHED contacts
     * <li>Clearing the RouteTable
     * <li>Sorting all ACTIVE contacts by their health and throwing away everything that is considered dead
     * <li>Sorting all CACHED contacts by their time (most recently seen to least recently seen) and adding them back
     * </ol>
     */
    public synchronized void rebuild() {
        ContactEntity[] active = getActiveContacts();
        ContactEntity[] cached = getCachedContacts();
        
        clear();
        
        // Sort the ACTIVE contacts by their health (most healthy to least healthy)
        // and exit the loop as soon as we encounter the first DEAD contact.
        ContactUtils.byHealth(active);
        for (ContactEntity entity : active) {
            if (entity.isDead()) {
                break;
            }
            
            add(entity.getContact());
        }
        
        // Sort the CACHED contacts by their time stamp (most recently encountered
        // to least recently encountered) and try to add them to the RouteTable.
        ContactUtils.byTimeStamp(cached);
        for (ContactEntity entity : cached) {
            add(entity.getContact());
        }
    }
    
    /**
     * Clears the {@link RouteTable}.
     */
    public synchronized void clear() {
        buckets.clear();
        init();
    }
    
    @Override
    public synchronized int size() {
        int size = 0;
        
        for (Bucket bucket : buckets.values()) {
            size += bucket.getActiveCount();
        }
        
        return size;
    }
    
    @Override
    public synchronized Bucket[] getBuckets() {
        return buckets.values().toArray(new Bucket[0]);
    }
    
    private class DefaultBucket extends AbstractBucket {
        
        private final Trie<KUID, ContactEntity> active;
        
        private final FixedSizeHashMap<KUID, ContactEntity> cached;
        
        private final NetworkCounter counter;
        
        private DefaultBucket(KUID bucketId, int depth) {
            super(bucketId, depth);
            
            active = new PatriciaTrie<KUID, ContactEntity>(keyAnalyzer);
            
            int maxCacheSize = settings.getMaxCacheSize();
            cached = new FixedSizeHashMap<KUID, ContactEntity>(
                    maxCacheSize, 1.0f, true, maxCacheSize);
            
            counter = new NetworkCounter(
                    settings.getNetworkMask());
        }
        
        @Override
        public int getActiveCount() {
            return active.size();
        }
        
        @Override
        public int getCachedCount() {
            return cached.size();
        }
        
        public int getMaxCacheSize() {
            return cached.getMaxSize();
        }
        
        public boolean isCacheFull() {
            return cached.isFull();
        }
        
        public boolean isActiveFull() {
            return active.size() >= settings.getK();
        }
        
        public Decision select(KUID contactId, 
                final Collection<Contact> dst, final int count) {
            
            final double probability = settings.getProbability();
            active.select(contactId, new Cursor<KUID, ContactEntity>() {
                @Override
                public Decision select(Entry<? extends KUID, 
                        ? extends ContactEntity> entry) {
                    
                    ContactEntity entity = entry.getValue();
                    
                    double random = 1.0d;
                    if (entity.isDead()) {
                        random = Math.random();
                    }
                    
                    if (random >= probability) {
                        dst.add(entity.getContact());
                    }
                    
                    return (dst.size() < count ? Decision.CONTINUE : Decision.EXIT);
                }
            });
            
            return (dst.size() < count ? Decision.CONTINUE : Decision.EXIT);
        }
        
        @Override
        public ContactEntity getActive(KUID contactId) {
            return active.get(contactId);
        }

        @Override
        public ContactEntity getCached(KUID contactId) {
            return cached.get(contactId);
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
        
        @Override
        public ContactEntity[] getActive() {
            return active.values().toArray(new ContactEntity[0]);
        }
        
        @Override
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
        
        public DefaultBucket[] split() {
            KUID bucketId = getBucketId();
            int depth = getDepth();
            
            DefaultBucket left = new DefaultBucket(bucketId, depth+1);
            DefaultBucket right = new DefaultBucket(bucketId.set(depth), depth+1);
            
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
            
            return new DefaultBucket[] { left, right };
        }
        
        @Override
        public boolean containsActive(KUID contactId) {
            return active.containsKey(contactId);
        }

        @Override
        public boolean containsCached(KUID contactId) {
            return cached.containsKey(contactId);
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
