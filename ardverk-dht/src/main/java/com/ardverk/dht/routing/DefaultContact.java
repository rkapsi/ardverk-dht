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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import org.ardverk.lang.Arguments;
import org.ardverk.net.NetworkUtils;

import com.ardverk.dht.KUID;

/**
 * 
 */
public class DefaultContact extends AbstractContact {
    
    private static final long serialVersionUID = 298059770472298142L;

    /**
     * Creates and returns a localhost {@link IContact}.
     */
    public static IContact localhost(KUID contactId, String address, int port) {
        return localhost(contactId, new InetSocketAddress(address, port));
    }
    
    /**
     * Creates and returns a localhost {@link IContact}.
     */
    public static IContact localhost(KUID contactId, InetAddress address, int port) {
        return localhost(contactId, new InetSocketAddress(address, port));
    }
    
    /**
     * Creates and returns a localhost {@link IContact}.
     */
    public static DefaultContact localhost(KUID contactId, SocketAddress address) {
        return new DefaultContact(Type.AUTHORITATIVE, contactId, 0, address);
    }
    
    private final Type type;
    
    private final long creationTime;
    
    private final long timeStamp;
    
    private final long rtt;
    
    private final int instanceId;
    
    private final SocketAddress socketAddress;
    
    private final SocketAddress contactAddress;
    
    private final SocketAddress remoteAddress;
    
    /**
     * Creates a {@link IContact}
     */
    public DefaultContact(Type type, 
            KUID contactId, 
            int instanceId, 
            SocketAddress address) {
        this(type, contactId, instanceId, address, address, 
                -1L, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Creates a {@link IContact}
     */
    public DefaultContact(Type type, 
            KUID contactId, 
            int instanceId, 
            SocketAddress address, 
            long rtt, TimeUnit unit) {
        this(type, contactId, instanceId, address, address, rtt, unit);
    }
    
    /**
     * Creates a {@link IContact}
     */
    public DefaultContact(Type type, 
            KUID contactId, 
            int instanceId, 
            SocketAddress socketAddress, 
            SocketAddress contactAddress) {
        this(type, contactId, instanceId, socketAddress, 
                contactAddress, -1L, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Creates a {@link IContact}
     */
    public DefaultContact(Type type, 
            KUID contactId, 
            int instanceId, 
            SocketAddress socketAddress, 
            SocketAddress contactAddress,
            long rtt, TimeUnit unit) {
        super(contactId);
        
        if (contactAddress == null) {
            contactAddress = socketAddress;
        }
        
        this.type = Arguments.notNull(type, "type");
        this.creationTime = System.currentTimeMillis();
        this.timeStamp = creationTime;
        this.rtt = unit.toMillis(rtt);
        
        this.instanceId = instanceId;
        this.socketAddress = Arguments.notNull(socketAddress, "socketAddress");
        this.contactAddress = Arguments.notNull(contactAddress, "contactAddress");
        this.remoteAddress = combine(socketAddress, contactAddress);
    }
    
    /**
     * 
     */
    protected DefaultContact(DefaultContact existing, int instanceId) {
        super(existing);
        
        this.creationTime = existing.creationTime;
        this.timeStamp = existing.timeStamp;
        this.rtt = existing.rtt;
        
        this.instanceId = instanceId;
        this.socketAddress = existing.socketAddress;
        this.contactAddress = existing.contactAddress;
        this.remoteAddress = existing.remoteAddress;
        
        this.type = existing.type;
    }
    
    protected DefaultContact(DefaultContact existing, long rtt, TimeUnit unit) {
        super(existing);
        
        this.creationTime = existing.creationTime;
        this.timeStamp = existing.timeStamp;
        this.rtt = unit.toMillis(rtt);
        
        this.instanceId = existing.instanceId;
        this.socketAddress = existing.socketAddress;
        this.contactAddress = existing.contactAddress;
        this.remoteAddress = existing.remoteAddress;
        
        this.type = existing.type;
    }
    
    protected DefaultContact(DefaultContact existing, 
            SocketAddress socketAddress, 
            SocketAddress contactAddress) {
        super(existing);
        
        this.creationTime = existing.creationTime;
        this.timeStamp = existing.timeStamp;
        this.rtt = existing.rtt;
        
        this.instanceId = existing.instanceId;
        this.socketAddress = Arguments.notNull(socketAddress, "socketAddress");
        this.contactAddress = Arguments.notNull(contactAddress, "contactAddress");
        this.remoteAddress = combine(socketAddress, contactAddress);
        
        this.type = existing.type;
    }
    
    /**
     * 
     */
    protected DefaultContact(DefaultContact existing, IContact o) {
        super(existing);
        
        if (!existing.equals(o)) {
            throw new IllegalArgumentException(existing + " vs. " + o);
        }
        
        DefaultContact other = (DefaultContact)o;
        
        // 2nd argument must be newer
        if (other.creationTime < existing.creationTime) {
            throw new IllegalArgumentException();
        }
        
        this.creationTime = existing.creationTime;
        
        if (other.isActive()) {
            this.timeStamp = other.timeStamp;
        } else {
            this.timeStamp = existing.timeStamp;
        }
        
        this.rtt = other.rtt >= 0L ? other.rtt : existing.rtt;
        
        if (existing.isBetter(other)) {
            this.instanceId = existing.instanceId;
            this.socketAddress = existing.socketAddress;
            this.contactAddress = existing.contactAddress;
            this.remoteAddress = existing.remoteAddress;
            this.type = existing.type;
        } else {
            this.instanceId = other.instanceId;
            this.socketAddress = other.socketAddress;
            this.contactAddress = other.contactAddress;
            this.remoteAddress = other.remoteAddress;
            this.type = other.type;
        }
    }
    
    /**
     * Returns {@code true} if this is a better {@link IContact} than
     * the other given {@link IContact}.
     */
    private boolean isBetter(DefaultContact other) {
        // Everything is a better than an *UNKNOWN* Contact even
        // if the other Contact is *UNKNOWN* too.
        return type != Type.UNKNOWN && isBetterOrEqual(other);
    }
    
    /**
     * Returns {@code true} if this is a better or a equally good 
     * {@link IContact} than the other given {@link IContact}.
     */
    private boolean isBetterOrEqual(DefaultContact other) {
        return type.isBetterOrEqual(other.type);
    }
    
    @Override
    public long getCreationTime() {
        return creationTime;
    }
    
    @Override
    public long getTimeStamp() {
        return timeStamp;
    }
    
    @Override
    public long getTimeSinceLastContact(TimeUnit unit) {
        long time = System.currentTimeMillis() - timeStamp;
        return unit.convert(time, TimeUnit.MILLISECONDS);
    }
    
    @Override
    public int getInstanceId() {
        return instanceId;
    }
    
    /**
     * Sets the {@link IContact}'s instance ID and returns a new {@link IContact}.
     */
    public IContact setInstanceId(int instanceId) {
        return this.instanceId != instanceId ? new DefaultContact(this, instanceId) : this;
    }
    
    @Override
    public SocketAddress getSocketAddress() {
        return socketAddress;
    }
    
    /**
     * Sets the {@link IContact}'s address as reported by the {@link Socket}.
     */
    public IContact setSocketAddress(SocketAddress address) {
        return new DefaultContact(this, address, contactAddress);
    }
    
    @Override
    public SocketAddress getContactAddress() {
        return contactAddress;
    }
    
    /**
     * Sets the {@link IContact}'s address as reported by the 
     * remote {@link IContact}.
     */
    public IContact setContactAddress(SocketAddress address) {
        return new DefaultContact(this, socketAddress, address);
    }
    
    @Override
    public SocketAddress getRemoteAddress() {
        return remoteAddress;
    }
    
    @Override
    public Type getType() {
        return type;
    }
    
    /**
     * Changes the {@link IContact}'s Round-Trip-Time (RTT)
     */
    public DefaultContact setRoundTripTime(long rtt, TimeUnit unit) {
        return new DefaultContact(this, rtt, unit);
    }
    
    @Override
    public long getRoundTripTime(TimeUnit unit) {
        return unit.convert(rtt, TimeUnit.MILLISECONDS);
    }
    
    @Override
    public IContact merge(IContact other) {
        return other != this ? new DefaultContact(this, other) : this;
    }
    
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        
        buffer.append("Type=").append(type)
            .append(", contactId=").append(contactId)
            .append(", instanceId=").append(instanceId)
            .append(", socketAddress=").append(socketAddress)
            .append(", contactAddress=").append(contactAddress)
            .append(", rtt=").append(rtt);
        return buffer.toString();
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
}