/*
 * Copyright 2009-2011 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ardverk.dht.routing;

import java.io.Serializable;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import org.ardverk.dht.lang.Identifier;


public interface Contact extends Identifier, Longevity, 
        Comparable<Contact>, RoundTripTime, Serializable {

    /**
     * The type of the {@link Contact}.
     */
    public static enum Type {
        /**
         * {@link Contact}s that were returned in FIND_NODE responses
         */
        UNKNOWN(0, false),
        
        /**
         * {@link Contact}s that sent us a request
         */
        UNSOLICITED(1, true),
        
        /**
         * {@link Contact}s that sent us a response
         */
        SOLICITED(2, true),
        
        /**
         * {@link Contact}s that have been created by the local user.
         */
        AUTHORITATIVE(3, true);
        
        private final int priority;
        
        private final boolean active;
        
        private Type(int priority, boolean active) {
            this.priority = priority;
            this.active = active;
        }
        
        public boolean isActive() {
            return active;
        }
        
        public int getPriority() {
            return priority;
        }
        
        public boolean isBetterOrEqual(Type other) {
            return priority >= other.priority;
        }
    }
    
    /**
     * Returns the {@link Type} of the {@link Contact}
     */
    public Type getType();
    
    /**
     * Returns {@code true} if the {@link Contact} is of the given {@link Type}
     */
    public boolean isType(Type type);
    
    /**
     * Returns {@code true} if this is a an authoritative {@link Contact}.
     */
    public boolean isAuthoritative();
    
    /**
     * Returns {@code true} if this {@link Contact} was discovered 
     * through solicited communication.
     */
    public boolean isSolicited();
    
    /**
     * Returns {@code true} if this {@link Contact} was discovered 
     * through unsolicited communication.
     */
    public boolean isUnsolicited();
    
    /**
     * Returns {@code true} if the {@link Contact} is considered active.
     * 
     * @see Type
     */
    public boolean isActive();
    
    /**
     * Returns {@code true} if the {@link Contact} is considered invisible
     * and shouldn't be added to the {@link RouteTable}.
     */
    public boolean isHidden();
    
    /**
     * Returns the {@link Contact}'s instance ID
     */
    public int getInstanceId();
    
    /**
     * Returns the {@link Contact}'s address as reported by 
     * the {@link Socket}.
     */
    public SocketAddress getSocketAddress();
    
    /**
     * Returns the {@link Contact}'s address as reported by 
     * the remote {@link Contact}.
     */
    public SocketAddress getContactAddress();
    
    /**
     * Returns the {@link Contact}'s remove address.
     * 
     * NOTE: This is the address we're using to send messages.
     */
    public SocketAddress getRemoteAddress();
    
    /**
     * Returns the amount of time in the given {@link TimeUnit} that 
     * has passed since we had contact with this {@link Contact}.
     */
    public long getTimeSinceLastContact(TimeUnit unit);
    
    /**
     * Returns the amount of time in milliseconds that has passed since 
     * we had contact with this {@link Contact}.
     */
    public long getTimeSinceLastContactInMillis();
    
    /**
     * Returns {@code true} if we haven't had any contact with this
     * {@link Contact} for the given period of time.
     * 
     * @see #getTimeSinceLastContact(TimeUnit)
     * @see #getTimeSinceLastContactInMillis()
     */
    public boolean isTimeout(long timeout, TimeUnit unit);
    
    /**
     * Merges this with the other {@link Contact}.
     */
    public Contact merge(Contact other);
}