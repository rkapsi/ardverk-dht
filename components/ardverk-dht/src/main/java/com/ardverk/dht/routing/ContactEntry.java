/*
 * Copyright 2009-2010 Roger Kapsi
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

import org.ardverk.lang.TimeStamp;
import org.ardverk.net.NetworkUtils;

import com.ardverk.dht.KUID;
import com.ardverk.dht.lang.Identifier;

/**
 * The {@link DefaultRouteTable} uses internally {@link ContactEntry}s 
 * instead of {@link Contact}s directly.
 * 
 * <p>NOTE: This class is <b>NOT</b> Thread-safe! All read/write operations
 * must be done while a lock on the {@link DefaultRouteTable} instance
 * is being held.
 * 
 * @see DefaultRouteTable
 */
public class ContactEntry implements Identifier, Longevity {
    
    private final RouteTableConfig config;
    
    private Contact contact;
    
    private int errorCount = 0;
    
    private long errorTimeStamp;
    
    ContactEntry(RouteTableConfig config, Contact contact) {
        this.config = config;
        this.contact = contact;
    }
    
    @Override
    public TimeStamp getCreationTime() {
        return contact.getCreationTime();
    }
    
    @Override
    public TimeStamp getTimeStamp() {
        return contact.getTimeStamp();
    }
    
    @Override
    public KUID getId() {
        return contact.getId();
    }
    
    /**
     * Returns the {@link ContactEntry}'s {@link Contact}.
     */
    public Contact getContact() {
        return contact;
    }
    
    /**
     * Updates the current {@link Contact} with the given {@link Contact}.
     */
    public Update update(Contact other) {
        Contact previous = contact;
        
        if (other.getCreationTime().compareTo(
                previous.getCreationTime()) >= 0) {
            contact = previous.merge(other);
        }
        
        if (other.isActive()) {
            errorCount = 0;
            errorTimeStamp = 0;
        }
        
        return new Update(previous, other, contact);
    }
    
    /**
     * Returns the number of errors this {@link ContactEntry} has
     * encountered.
     */
    public int getErrorCount() {
        return errorCount;
    }
    
    /**
     * Returns the time when the most recent error occurred.
     */
    public long getErrorTimeStamp() {
        return errorTimeStamp;
    }
    
    /**
     * Increments the error count, sets the error time stamp and
     * returns {@code true} if the {@link Contact} is considered
     * dead.
     */
    public boolean error() {
        ++errorCount;
        errorTimeStamp = System.currentTimeMillis();
        return isDead();
    }
    
    /**
     * @see DefaultContact#isSolicited()
     */
    public boolean isSolicited() {
        return contact.isSolicited();
    }
    
    /**
     * @see DefaultContact#isUnsolicited()
     */
    public boolean isUnsolicited() {
        return contact.isUnsolicited();
    }
    
    /**
     * Returns {@code true} if the error count has exceeded the maximum error 
     * count as defined in {@link RouteTableConfig#getMaxContactErrors()}.
     */
    public boolean isDead() {
        return errorCount >= config.getMaxContactErrors();
    }
    
    /**
     * Returns {@code true} if the {@link Contact} is not dead
     * and active.
     * 
     * @see #isDead()
     * @see DefaultContact#isActive()
     */
    public boolean isAlive() {
        return !isDead() && contact.isActive();
    }
    
    /**
     * Returns {@code true} if the {@link Contact} is not dead
     * but unsolicited.
     * 
     * @see #isDead()
     * @see #isUnsolicited()
     */
    public boolean isUnknown() {
        return !isDead() && isUnsolicited();
    }
    
    /**
     * Returns {@code true} if the {@link Contact} has been recently active as 
     * defined in {@link RouteTableConfig#getHasBeenActiveTimeoutInMillis()}.
     */
    public boolean hasBeenActiveRecently() {
        long timeout = config.getHasBeenActiveTimeoutInMillis();
        return getTimeStamp().getAgeInMillis() < timeout;
    }
    
    /**
     * Returns {@code true} if both {@link Contact}s are equal as defined in
     * {@link DefaultContact#equals(Object)}.
     */
    public boolean isSameContact(Contact other) {
        return contact.equals(other);
    }
    
    /**
     * Returns {@code true} if both {@link Contact}s have the same
     * remote {@link SocketAddress}.
     * 
     * @see DefaultContact#getRemoteAddress()
     */
    public boolean isSameRemoteAddress(Contact contact) {
        return NetworkUtils.isSameAddress(
                this.contact.getRemoteAddress(), 
                contact.getRemoteAddress());
    }
    
    public static class Update {
        
        private final Contact previous;
        
        private final Contact other;
        
        private final Contact merged;

        private Update(Contact previous, Contact other, Contact merged) {
            this.previous = previous;
            this.other = other;
            this.merged = merged;
        }

        public Contact getPrevious() {
            return previous;
        }

        public Contact getOther() {
            return other;
        }

        public Contact getMerged() {
            return merged;
        }
    }
}