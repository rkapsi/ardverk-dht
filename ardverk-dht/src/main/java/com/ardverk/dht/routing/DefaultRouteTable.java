/*
 * Copyright 2010 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ardverk.dht.routing;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import org.ardverk.collection.Cursor;
import org.ardverk.collection.Cursor.Decision;
import org.ardverk.collection.FixedSizeHashMap;
import org.ardverk.collection.PatriciaTrie;
import org.ardverk.collection.Trie;
import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncFutureListener;
import org.ardverk.concurrent.FutureUtils;
import org.ardverk.lang.Arguments;
import org.ardverk.lang.NullArgumentException;
import org.ardverk.net.NetworkCounter;
import org.slf4j.Logger;

import com.ardverk.dht.KUID;
import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.config.PingConfig;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.lang.Identifier;
import com.ardverk.dht.logging.LoggerUtils;
import com.ardverk.dht.utils.ContactKey;

public class DefaultRouteTable extends AbstractRouteTable {
    
    private static final Logger LOG 
        = LoggerUtils.getLogger(DefaultRouteTable.class);
    
    private final Map<ContactKey, ArdverkFuture<PingEntity>> pingFutures 
        = new HashMap<ContactKey, ArdverkFuture<PingEntity>>();
    
    private final RouteTableConfig config;
    
    private final Contact localhost;
    
    private final Trie<KUID, DefaultBucket> buckets;
    
    private int consecutiveErrors = 0;
    
    public DefaultRouteTable(Contact localhost) {
        this(new RouteTableConfig(), localhost);
    }
    
    public DefaultRouteTable(int k, Contact localhost) {
        this(new RouteTableConfig(k), localhost);
    }
    
    public DefaultRouteTable(RouteTableConfig config, Contact localhost) {
        this.config = Arguments.notNull(config, "config");
        this.localhost = Arguments.notNull(localhost, "localhost");
        
        this.buckets = new PatriciaTrie<KUID, DefaultBucket>();
        
        init();
    }
    
    private synchronized void init() {
        consecutiveErrors = 0;
        
        KUID contactId = localhost.getId();
        KUID bucketId = contactId.min();
        
        DefaultBucket bucket = new DefaultBucket(bucketId, 0);
        buckets.put(bucketId, bucket);
        
        add0(localhost);
    }
    
    /**
     * Returns the {@link DefaultRouteTable}'s {@link RouteTableConfig}.
     */
    public RouteTableConfig getRouteTableConfig() {
        return config;
    }
    
    @Override
    public Contact getLocalhost() {
        return localhost;
    }
    
    private boolean isLocalhost(ContactEntity entity) {
        return isLocalhost(entity.getContact());
    }
    
    private boolean isLocalhost(Contact contact) {
        return localhost.getId().equals(contact.getId());
    }
    
    private void checkKeyLength(Contact other) throws IllegalArgumentException {
        KUID contactId = localhost.getId();
        KUID otherId = other.getId();
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
        KUID contactId = contact.getId();
        DefaultBucket bucket = buckets.selectValue(contactId);
        ContactEntity entity = bucket.get(contactId);
        
        if (contact.isAuthoritative()) {
            authoritative(bucket, entity, contact);
            return;
        }
        
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
    
    private synchronized void authoritative(DefaultBucket bucket, 
            ContactEntity entity, Contact contact) {
        
        assert (contact.isAuthoritative());
        
        if (entity != null) {
            ContactEntity removed = bucket.remove(entity);
            assert (removed == entity);
        }
        
        if (bucket.isActiveFull()) {
            ContactEntity lrs = bucket.getLeastRecentlySeenActiveContact();
            ContactEntity removed = bucket.removeActive(lrs);
            assert (removed == lrs);
            
            if (!lrs.isDead()) {
                bucket.addCache(lrs);
            }
        }
        
        bucket.addActive(new ContactEntity(config, contact));
    }
    
    private synchronized void updateContact(DefaultBucket bucket, 
            ContactEntity entity, Contact contact) {
        
        // Make sure neither is the localhost!
        assert (!entity.isSameContact(localhost) 
                && !contact.equals(localhost));
        
        // Make sure non-ACTIVE contacts can never 
        // replace an ACTIVE contact!
        if (entity.isAlive() && !contact.isActive()) {
            return;
        }
        
        // Everything is fine if they've got the same address.
        if (entity.isSameRemoteAddress(contact)) {
            update(bucket, entity, contact);
        } else {
            checkContact(bucket, entity, contact);
        }
    }
    
    private synchronized void checkContact(DefaultBucket bucket, 
            ContactEntity entity, final Contact contact) {
        
        if (config.isCheckIdentity()) {
            
            final Contact previous = entity.getContact();
            
            ArdverkFuture<PingEntity> future = ping(entity);
            future.addAsyncFutureListener(new AsyncFutureListener<PingEntity>() {
                @Override
                public void operationComplete(AsyncFuture<PingEntity> future) {
                    // Do nothing if there was *NO* error (in other words if
                    // we received a PONG). We're simply dropping the new 
                    // Contact's information!
                    if (!future.isCompletedAbnormally()) {
                        try {
                            Contact contact = future.get().getContact();
                            fireContactCollision(previous, contact);
                        } catch (InterruptedException e) {
                            LOG.error("InterruptedException", e);
                        } catch (ExecutionException e) {
                            LOG.error("ExecutionException", e);
                        }
                        return;
                    }
                    
                    // Cancellations are OK too
                    if (future.isCancelled()) {
                        return;
                    }
                    
                    KUID contactId = contact.getId();
                    
                    synchronized (DefaultRouteTable.this) {
                        DefaultBucket bucket = buckets.selectValue(contactId);
                        ContactEntity current = bucket.get(contactId);
                        
                        // Make sure the pre-condition still holds and we're
                        // not replacing some other Contact.
                        if (current != null && current.getContact() == previous) {
                            update(bucket, current, contact);
                            
                            if (bucket.containsCached(contactId)) {
                                pingLeastRecentlySeenContact(bucket);
                            }
                            
                        } else {
                            add(contact);
                        }
                    }
                }
            });
        } else {
            replace(bucket, entity, contact);
            
            if (bucket.containsCached(contact.getId())) {
                pingLeastRecentlySeenContact(bucket);
            }
        }
    }
    
    private synchronized boolean isOkayToAdd(DefaultBucket bucket, ContactEntity entity) {
        return isOkayToAdd(bucket, entity.getContact());
    }
    
    private synchronized boolean isOkayToAdd(DefaultBucket bucket, Contact contact) {
        return isOkayToAdd(bucket, contact.getRemoteAddress());
    }
    
    private synchronized boolean isOkayToAdd(DefaultBucket bucket, SocketAddress remoteAddress) {
        return bucket.getContactCount(remoteAddress) 
                < config.getMaxContactsFromSameNetwork();
    }
    
    private synchronized void addActive(DefaultBucket bucket, Contact contact) {
        ContactEntity entity = new ContactEntity(config, contact);
        boolean success = bucket.addActive(entity);
        
        if (success) {
            fireContactAdded(bucket, contact);
        }
    }
    
    private synchronized ContactEntity addCache(DefaultBucket bucket, Contact contact) {
        ContactEntity entity = new ContactEntity(config, contact);
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
        if (contact.isActive() && isOkayToAdd(bucket, contact)) {
            ContactEntity lrs = bucket.getLeastRecentlySeenActiveContact();
            
            if (!isLocalhost(lrs) && (lrs.isUnknown() || lrs.isDead())) {
                
                ContactEntity entity = bucket.removeActive(lrs);
                assert (entity == lrs);
                
                bucket.addActive(new ContactEntity(config, contact));
                
                fireContactReplaced(bucket, lrs.getContact(), contact);
                return;
            }
        }
        
        addCache(bucket, contact);
        pingLeastRecentlySeenContact(bucket);
    }
    
    private synchronized void update(DefaultBucket bucket, 
            ContactEntity entity, Contact contact) {
        ContactEntity.Update update = entity.update(contact);
        bucket.touch();
        
        fireContactChanged(bucket, 
                update.getPrevious(), update.getMerged());
    }
    
    private synchronized void replace(DefaultBucket bucket, 
            ContactEntity entity, Contact contact) {
        Contact existing = entity.replace(contact);
        bucket.touch();
        
        fireContactChanged(bucket, existing, contact);
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
            DefaultBucket oldLeft = buckets.put(left.getId(), left);
            assert (oldLeft == bucket);
            
            // The right one is new in the RouteTable
            DefaultBucket oldRight = buckets.put(right.getId(), right);
            assert (oldRight == null);
            
            fireBucketSplit(bucket, left, right);
            return true;
        }
        
        return false;
    }
    
    private synchronized boolean canSplit(DefaultBucket bucket) {
        
        // We *split* the Bucket if:
        // 1. Bucket contains the localhost Contact
        // 2. Bucket is smallest subtree
        // 3. Bucket hasn't reached its max depth
        KUID contactId = localhost.getId();
        
        if (bucket.contains(contactId)
                || isSmallestSubtree(bucket)
                || !isTooDeep(bucket)) {
            return true;
        }
        return false;
    }
    
    /**
     * Returns true if the given {@link DefaultBucket} has reached its maximum
     * depth in the RoutingTable Tree.
     */
    private synchronized boolean isTooDeep(DefaultBucket bucket) {
        return bucket.getDepth() >= config.getMaxDepth();
    }
    
    /**
     * Returns true if the given {@link DefaultBucket} is the closest left
     * or right hand sibling of the {@link DefaultBucket} which contains 
     * the localhost {@link Contact}.
     */
    private synchronized boolean isSmallestSubtree(DefaultBucket bucket) {
        KUID contactId = localhost.getId();
        KUID bucketId = bucket.getId();
        int prefixLength = contactId.commonPrefix(bucketId);
        
        // The sibling Bucket contains the localhost Contact. 
        // We're looking if the other Bucket is its sibling 
        // (what we call the smallest subtree).
        DefaultBucket sibling = buckets.selectValue(contactId);
        return (sibling.getDepth() - 1) == prefixLength;
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
        return config.getK();
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
    
    private synchronized ArdverkFuture<PingEntity> ping(ContactEntity entity) {
        Contact contact = entity.getContact();
        
        // Make sure we're not pinging the same host in parallel.
        // It is an unlikely but possible case...
        final ContactKey pingKey = new ContactKey(contact);
        ArdverkFuture<PingEntity> future 
            = pingFutures.get(pingKey);
        
        if (future == null) {
            PingConfig pingConfig = config.getPingConfig();
            future = ping(contact, pingConfig);
            
            future.addAsyncFutureListener(new AsyncFutureListener<PingEntity>() {
                @Override
                public void operationComplete(AsyncFuture<PingEntity> future) {
                    synchronized (DefaultRouteTable.this) {
                        pingFutures.remove(pingKey);
                    }
                }
            });
            pingFutures.put(pingKey, future);
        }
        
        return future;
    }
    
    @Override
    public synchronized void handleIoError(KUID contactId, SocketAddress address) {
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
        if (++consecutiveErrors >= config.getMaxConsecutiveErrors()) {
            return;
        }
        
        boolean dead = entity.error();
        if (dead) {
            
            if (bucket.containsActive(contactId)) {
                
                // Remove or replace Contacts in the *ACTIVE* RouteTable
                // only if there is something in the replacement cache or
                // if the Contact has just too many errors and there is
                // simply no point in keeping it in the RouteTable.
                
                if (!bucket.isCacheEmpty()) {
                    ContactEntity mrs = null;
                    while ((mrs = bucket.getMostRecentlySeenCachedContact()) != null) {
                        ContactEntity removed = bucket.removeCache(mrs);
                        assert (removed == mrs);
                        
                        if (isOkayToAdd(bucket, mrs)) {
                            removed = bucket.removeActive(entity);
                            assert (removed == entity 
                                    && !bucket.isActiveFull());
                            
                            bucket.addActive(mrs);
                            fireContactReplaced(bucket, 
                                    entity.getContact(), mrs.getContact());
                            break;
                        }
                    }
                } else if (entity.getErrorCount() 
                        >= config.getTooManyErrorsCount()) {
                    ContactEntity removed = bucket.removeActive(entity);
                    assert(removed == entity && !bucket.isActiveFull());
                    
                    fireContactRemoved(bucket, entity.getContact());
                }
                
            } else {
                
                // This looks strange as Contacts are never selected from the 
                // RouteTable but it's however possible that FIND_NODE responses 
                // return Contacts that happen to be in our RouteTable's cache 
                // and if that's the case we want to remove them ASAP.
                
                ContactEntity removed = bucket.removeCache(contactId);
                assert (removed == entity);
            }
        }
    }
    
    /**
     * Returns all ACTIVE {@link ContactEntity}s.
     */
    public synchronized ContactEntity[] getActiveContacts() {
        return getContacts(true);
    }

    /**
     * Returns all CACHED {@link ContactEntity}s.
     */
    public synchronized ContactEntity[] getCachedContacts() {
        return getContacts(false);
    }

    /**
     * Returns {@link ContactEntity}ies of the given {@link ContactType}.
     */
    private ContactEntity[] getContacts(boolean active) {
        List<ContactEntity> contacts = new ArrayList<ContactEntity>();
        for (DefaultBucket bucket : buckets.values()) {
            
            ContactEntity[] entitis = active ? bucket.getActive() : bucket.getCached();
            
            for (ContactEntity entity : entitis) {
                contacts.add(entity);
            }
        }
        
        return contacts.toArray(new ContactEntity[0]);
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
        LongevityUtils.byTimeStamp(cached);
        for (ContactEntity entity : cached) {
            add(entity.getContact());
        }
    }
    
    /**
     * Clears the {@link RouteTable}.
     */
    public synchronized void clear() {
        FutureUtils.cancelAll(pingFutures.values(), true);
        pingFutures.clear();
        
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
    
    @Override
    public synchronized String toString() {
        StringBuilder buffer = new StringBuilder();
        
        int bucketIndex = 0;
        for (Bucket bucket : getBuckets()) {
            buffer.append(bucketIndex++).append(")")
                .append(bucket.getId()).append("[")
                .append(bucket.getDepth()).append("]\n");
            
            int contactIndex = 0;
            for (ContactEntity entity : bucket.getActive()) {
                buffer.append(" ").append(contactIndex++).append(") ")
                    .append(entity.getContact()).append("\n");
            }
        }
        
        return buffer.toString();
    }
    
    private class DefaultBucket extends AbstractBucket {
        
        private final Trie<KUID, ContactEntity> active;
        
        private final FixedSizeHashMap<KUID, ContactEntity> cached;
        
        private final NetworkCounter counter;
        
        private DefaultBucket(KUID bucketId, int depth) {
            super(bucketId, depth);
            
            active = new PatriciaTrie<KUID, ContactEntity>();
            
            int maxCacheSize = config.getMaxCacheSize();
            cached = new FixedSizeHashMap<KUID, ContactEntity>(
                    maxCacheSize, maxCacheSize);
            
            counter = new NetworkCounter(
                    config.getNetworkMask());
        }
        
        @Override
        public int getActiveCount() {
            return active.size();
        }
        
        @Override
        public int getCachedCount() {
            return cached.size();
        }
        
        @Override
        public boolean containsActive(KUID contactId) {
            return active.containsKey(contactId);
        }

        @Override
        public boolean containsCached(KUID contactId) {
            return cached.containsKey(contactId);
        }
        
        @Override
        public ContactEntity getActive(KUID contactId) {
            return active.get(contactId);
        }

        @Override
        public ContactEntity getCached(KUID contactId) {
            return cached.get(contactId);
        }

        @Override
        public ContactEntity[] getActive() {
            return active.values().toArray(new ContactEntity[0]);
        }
        
        @Override
        public ContactEntity[] getCached() {
            return cached.values().toArray(new ContactEntity[0]);
        }
        
        /**
         * Returns {@code true} if the {@link Bucket}'s cache is full.
         */
        private boolean isCacheFull() {
            return cached.isFull();
        }
        
        /**
         * Returns {@code true} if the {@link Bucket} is full.
         */
        private boolean isActiveFull() {
            return active.size() >= config.getK();
        }
        
        /**
         * Selects and adds {@link Contact}s by their XOR distance to the
         * given {@link Collection} until its max capacity has been reached.
         */
        private Decision select(KUID contactId, 
                final Collection<Contact> dst, final int count) {
            
            final double probability = config.getProbability();
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
        
        /**
         * Returns the number of {@link Contact}s in the {@link Bucket}'s 
         * active list that are in the same network as the given 
         * {@link SocketAddress}.
         */
        private int getContactCount(SocketAddress address) {
            return counter.get(address);
        }
        
        /**
         * Adds the given {@link ContactEntity} to the {@link Bucket}.
         */
        private void add(ContactEntity entity) {
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
        
        /**
         * Adds the given {@link ContactEntity} to the {@link Bucket}'s active list.
         */
        private boolean addActive(ContactEntity entity) {
            KUID contactId = entity.getId();
            
            // Make sure Bucket does not contain the Contact!
            assert (!contains(contactId));
                
            if (hasOrMakeSpace()) {
                active.put(contactId, entity);
                
                Contact contact = entity.getContact();
                counter.add(contact.getRemoteAddress());
                touch();
                return true;
            }
            
            return false;
        }
        
        /**
         * Adds the {@link ContactEntity} to the {@link Bucket}'s cache list.
         */
        private ContactEntity addCache(ContactEntity entity) {
            Contact contact = entity.getContact();
            KUID contactId = contact.getId();
            
            // Make sure Bucket does not contain the Contact!
            assert (!contains(contactId));
            
            if (!isCacheFull()) {
                cached.put(contactId, entity);
                return entity;
            }
            
            ContactEntity lrs = getLeastRecentlySeenCachedContact();
            if (lrs.isDead() || (!lrs.hasBeenActiveRecently() && !entity.isDead())) {
                ContactEntity removed = cached.remove(lrs.getId());
                assert (lrs == removed);
                
                cached.put(contactId, entity);
                return removed;
            }
            
            return null;
        }
        
        /**
         * Returns the least recently seen {@link ContactEntity} in 
         * the {@link Bucket}'s cache list.
         */
        private ContactEntity getLeastRecentlySeenCachedContact() {
            return ContactUtils.getLeastRecentlySeen(cached.values());
        }
        
        /**
         * Returns the least recently seen {@link ContactEntity} in 
         * the {@link Bucket}'s active list.
         */
        private ContactEntity getLeastRecentlySeenActiveContact() {
            return ContactUtils.getLeastRecentlySeen(active.values());
        }
        
        /**
         * Returns the most recently seen {@link ContactEntity} in 
         * the {@link Bucket}'s cache list.
         */
        private ContactEntity getMostRecentlySeenCachedContact() {
            return ContactUtils.getMostRecentlySeen(cached.values());
        }
        
        /**
         * Returns {@code true} if the {@link Bucket} has or was able
         * to make space in the active list.
         */
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
        
        /**
         * Removes the given {@link Identifier} from the {@link Bucket}.
         */
        private ContactEntity remove(Identifier identifer) {
            ContactEntity entity = removeActive(identifer);
            if (entity == null) {
                entity = removeCache(identifer);
            }
            return entity;
        }
        
        /**
         * Removes the given {@link Identifier} from the {@link Bucket}'s active list.
         */
        private ContactEntity removeActive(Identifier identifier) {
            ContactEntity entity = active.remove(identifier.getId());
            
            Contact contact = entity.getContact();
            SocketAddress address = contact.getRemoteAddress();
            counter.remove(address);
            
            return entity;
        }
        
        /**
         * Removes the given {@link Identifier} from the {@link Bucket}'s cache list.
         */
        private ContactEntity removeCache(Identifier identifier) {
            return cached.remove(identifier.getId());
        }
        
        /**
         * Splits the {@link Bucket} in two.
         */
        private DefaultBucket[] split() {
            KUID bucketId = getId();
            int depth = getDepth();
            
            DefaultBucket left = new DefaultBucket(bucketId, depth+1);
            DefaultBucket right = new DefaultBucket(bucketId.set(depth), depth+1);
            
            for (ContactEntity entity : active.values()) {
                KUID contactId = entity.getId();
                
                if (!contactId.isBitSet(depth)) {
                    left.add(entity);
                } else {
                    right.add(entity);
                }
            }
            
            for (ContactEntity entity : cached.values()) {
                KUID contactId = entity.getId();
                if (!contactId.isBitSet(depth)) {
                    left.add(entity);
                } else {
                    right.add(entity);
                }
            }
            
            return new DefaultBucket[] { left, right };
        }
    }
}