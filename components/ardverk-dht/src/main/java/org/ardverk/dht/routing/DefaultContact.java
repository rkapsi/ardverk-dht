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

package org.ardverk.dht.routing;

import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import org.ardverk.dht.KUID;
import org.ardverk.lang.Arguments;
import org.ardverk.lang.TimeStamp;
import org.ardverk.net.NetworkUtils;


/**
 * 
 */
public class DefaultContact extends AbstractContact {
    
    private static final long serialVersionUID = 298059770472298142L;
    
    private final Type type;
    
    private final TimeStamp creationTime;
    
    private final TimeStamp timeStamp;
    
    private final int instanceId;
    
    private final boolean invisible;
    
    private final SocketAddress socketAddress;
    
    private final SocketAddress contactAddress;
    
    private final SocketAddress remoteAddress;
    
    /**
     * Creates a {@link DefaultContact}
     */
    public DefaultContact(KUID contactId, SocketAddress address) {
        this(Type.UNKNOWN, contactId, 0, false, address);
    }
    
    /**
     * Creates a {@link DefaultContact}
     */
    public DefaultContact(Type type, KUID contactId, 
            int instanceId, boolean invisible, 
            SocketAddress address) {
        this(type, contactId, instanceId, invisible, address, address);
    }
    
    /**
     * Creates a {@link DefaultContact}
     */
    public DefaultContact(Type type, KUID contactId, 
            int instanceId, boolean invisible, 
            SocketAddress socketAddress, SocketAddress contactAddress) {
        this(type, contactId, instanceId, invisible, 
                socketAddress, contactAddress, -1L, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Creates a {@link DefaultContact}
     */
    public DefaultContact(Type type, 
            KUID contactId, 
            int instanceId, 
            boolean invisible,
            SocketAddress socketAddress, 
            SocketAddress contactAddress,
            long rtt, TimeUnit unit) {
        super(contactId, rtt, unit);
        
        if (contactAddress == null) {
            contactAddress = socketAddress;
        }
        
        this.type = Arguments.notNull(type, "type");
        this.creationTime = TimeStamp.now();
        this.timeStamp = creationTime;
        
        this.instanceId = instanceId;
        this.invisible = invisible;
        this.socketAddress = Arguments.notNull(socketAddress, "socketAddress");
        this.contactAddress = Arguments.notNull(contactAddress, "contactAddress");
        this.remoteAddress = combine(socketAddress, contactAddress);
    }
    
    /**
     * 
     */
    private DefaultContact(DefaultContact existing, Contact other) {
        super(existing, pickRTT(existing, other), TimeUnit.MILLISECONDS);
        
        this.creationTime = existing.getCreationTime();
        
        if (other.isActive()) {
            this.timeStamp = other.getTimeStamp();
        } else {
            this.timeStamp = existing.getTimeStamp();
        }
        
        if (existing.isBetter(other)) {
            this.instanceId = existing.getInstanceId();
            this.invisible = existing.isInvisible();
            this.socketAddress = existing.getSocketAddress();
            this.contactAddress = existing.getContactAddress();
            this.remoteAddress = existing.getRemoteAddress();
            this.type = existing.getType();
        } else {
            this.instanceId = other.getInstanceId();
            this.invisible = other.isInvisible();
            this.socketAddress = other.getSocketAddress();
            this.contactAddress = other.getContactAddress();
            this.remoteAddress = other.getRemoteAddress();
            this.type = other.getType();
        }
    }
    
    /**
     * Returns {@code true} if this is a better {@link Contact} than
     * the other given {@link Contact}.
     */
    private boolean isBetter(Contact other) {
        // Everything is a better than an *UNKNOWN* Contact even
        // if the other Contact is *UNKNOWN* too.
        return type != Type.UNKNOWN && isBetterOrEqual(other);
    }
    
    /**
     * Returns {@code true} if this is a better or a equally good 
     * {@link Contact} than the other given {@link Contact}.
     */
    private boolean isBetterOrEqual(Contact other) {
        return type.isBetterOrEqual(other.getType());
    }
    
    @Override
    public TimeStamp getCreationTime() {
        return creationTime;
    }
    
    @Override
    public TimeStamp getTimeStamp() {
        return timeStamp;
    }
    
    @Override
    public int getInstanceId() {
        return instanceId;
    }
    
    @Override
    public boolean isInvisible() {
        return invisible;
    }
    
    @Override
    public SocketAddress getSocketAddress() {
        return socketAddress;
    }
    
    @Override
    public SocketAddress getContactAddress() {
        return contactAddress;
    }
    
    @Override
    public SocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    @Override
    public Type getType() {
        return type;
    }
    
    @Override
    public Contact merge(Contact other) {
        if (!equals(other) || other.isInvisible()) {
            throw new IllegalArgumentException("other=" + other);
        }
        
        return other != this ? new DefaultContact(this, other) : this;
    }
    
    /**
     * Combines the socket addresses {@link InetAddress} and the
     * contact addresses port number.
     */
    private static SocketAddress combine(SocketAddress socketAddress, 
            SocketAddress contactAddress) {
        String host = NetworkUtils.getHostName(socketAddress);
        int port = NetworkUtils.getPort(contactAddress);
        return NetworkUtils.createUnresolved(host, port);
    }
    
    /**
     * Picks and returns the RTT for the given two {@link Contact}s.
     */
    private static long pickRTT(Contact existing, Contact other) {
        long otherRTT = other.getRoundTripTimeInMillis();
        return otherRTT >= 0L ? otherRTT : existing.getRoundTripTimeInMillis();
    }
}