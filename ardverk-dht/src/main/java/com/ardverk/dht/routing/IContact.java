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

import java.io.Serializable;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import com.ardverk.dht.lang.Identifier;

public interface IContact extends Identifier, Longevity, 
        Comparable<IContact>, Serializable {

    /**
     * 
     */
    public static enum Type {
        /**
         * {@link IContact}s that were returned in FIND_NODE responses
         */
        UNKNOWN(0, false),
        
        /**
         * {@link IContact}s that sent us a request
         */
        UNSOLICITED(1, true),
        
        /**
         * {@link IContact}s that sent us a response
         */
        SOLICITED(2, true),
        
        /**
         * {@link IContact}s that have been created by the local user.
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
     * Returns the {@link Type} of the {@link IContact}
     */
    public Type getType();
    
    /**
     * Returns {@code true} if the {@link IContact} is of the given {@link Type}
     */
    public boolean isType(Type type);
    
    /**
     * Returns {@code true} if this is a an authoritative {@link IContact}.
     */
    public boolean isAuthoritative();
    
    /**
     * Returns {@code true} if this {@link IContact} was discovered 
     * through solicited communication.
     */
    public boolean isSolicited();
    
    /**
     * Returns {@code true} if this {@link IContact} was discovered 
     * through unsolicited communication.
     */
    public boolean isUnsolicited();
    
    /**
     * Returns true if the {@link IContact} is considered active.
     * 
     * @see Type
     */
    public boolean isActive();
    
    /**
     * Returns the {@link IContact}'s instance ID
     */
    public int getInstanceId();
    
    /**
     * Returns the {@link IContact}'s address as reported by 
     * the {@link Socket}.
     */
    public SocketAddress getSocketAddress();
    
    /**
     * Returns the {@link IContact}'s address as reported by 
     * the remote {@link IContact}.
     */
    public SocketAddress getContactAddress();
    
    /**
     * Returns the {@link IContact}'s remove address.
     * 
     * NOTE: This is the address we're using to send messages.
     */
    public SocketAddress getRemoteAddress();
    
    /**
     * Returns the {@link IContact}'s Round-Trip-Time (RTT) or a negative 
     * value if the RTT is unknown.
     */
    public long getRoundTripTime(TimeUnit unit);
    
    /**
     * Returns the {@link IContact}'s Round-Trip-Time (RTT) in milliseconds
     * or a negative value if the RTT is unknown.
     */
    public long getRoundTripTimeInMillis();
    
    /**
     * 
     */
    public IContact setRoundTripTime(long rtt, TimeUnit unit);
    
    /**
     * Returns the amount of time in the given {@link TimeUnit} that 
     * has passed since we had IContact with this {@link IContact}.
     */
    public long getTimeSinceLastContact(TimeUnit unit);
    
    /**
     * Returns the amount of time in milliseconds that has passed since 
     * we had IContact with this {@link IContact}.
     */
    public long getTimeSinceLastContactInMillis();
    
    /**
     * Returns {@code true} if we haven't had any contact with this
     * {@link IContact} for the given period of time.
     * 
     * @see #getTimeSinceLastContact(TimeUnit)
     * @see #getTimeSinceLastContactInMillis()
     */
    public boolean isTimeout(long timeout, TimeUnit unit);
    
    /**
     * Returns the adaptive timeout for this {@link IContact}.
     */
    public long getAdaptiveTimeout(double multiplier, 
            long defaultTimeout, TimeUnit unit);
    
    /**
     * Merges this with the other {@link IContact}.
     */
    public IContact merge(IContact other);
}