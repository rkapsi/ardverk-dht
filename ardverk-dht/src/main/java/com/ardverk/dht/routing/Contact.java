package com.ardverk.dht.routing;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import org.ardverk.lang.Arguments;
import org.ardverk.lang.NullArgumentException;
import org.ardverk.net.NetworkUtils;

import com.ardverk.dht.KUID;
import com.ardverk.dht.lang.Identifier;

/**
 * 
 */
public class Contact implements Identifier, Longevity, Comparable<Contact>, Serializable {
    
    private static final long serialVersionUID = 298059770472298142L;

    /**
     * 
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
     * Creates and returns a localhost {@link Contact}.
     */
    public static Contact localhost(KUID contactId, String address, int port) {
        return localhost(contactId, new InetSocketAddress(address, port));
    }
    
    /**
     * Creates and returns a localhost {@link Contact}.
     */
    public static Contact localhost(KUID contactId, InetAddress address, int port) {
        return localhost(contactId, new InetSocketAddress(address, port));
    }
    
    /**
     * Creates and returns a localhost {@link Contact}.
     */
    public static Contact localhost(KUID contactId, SocketAddress address) {
        return new Contact(Type.AUTHORITATIVE, contactId, 0, address);
    }
    
    private final long creationTime;
    
    private final long timeStamp;
    
    private final long rtt;
    
    private final Type type;
    
    private final KUID contactId;
    
    private final int instanceId;
    
    private final SocketAddress socketAddress;
    
    private final SocketAddress contactAddress;
    
    private final SocketAddress remoteAddress;
    
    /**
     * Creates a {@link Contact}
     */
    public Contact(Type type, 
            KUID contactId, 
            int instanceId, 
            SocketAddress address) {
        this(type, contactId, instanceId, address, address, 
                -1L, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Creates a {@link Contact}
     */
    public Contact(Type type, 
            KUID contactId, 
            int instanceId, 
            SocketAddress address, 
            long rtt, TimeUnit unit) {
        this(type, contactId, instanceId, address, address, rtt, unit);
    }
    
    /**
     * Creates a {@link Contact}
     */
    public Contact(Type type, 
            KUID contactId, 
            int instanceId, 
            SocketAddress socketAddress, 
            SocketAddress contactAddress) {
        this(type, contactId, instanceId, socketAddress, 
                contactAddress, -1L, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Creates a {@link Contact}
     */
    public Contact(Type type, 
            KUID contactId, 
            int instanceId, 
            SocketAddress socketAddress, 
            SocketAddress contactAddress,
            long rtt, TimeUnit unit) {
        
        if (contactAddress == null) {
            contactAddress = socketAddress;
        }
        
        this.type = Arguments.notNull(type, "type");
        this.creationTime = System.currentTimeMillis();
        this.timeStamp = creationTime;
        this.rtt = unit.toMillis(rtt);
        
        this.contactId = Arguments.notNull(contactId, "contactId");
        this.instanceId = instanceId;
        this.socketAddress = Arguments.notNull(socketAddress, "socketAddress");
        this.contactAddress = Arguments.notNull(contactAddress, "contactAddress");
        this.remoteAddress = combine(socketAddress, contactAddress);
    }
    
    /**
     * 
     */
    protected Contact(Contact existing, int instanceId) {
        if (existing == null) {
            throw new NullArgumentException("existing");
        }
        
        this.creationTime = existing.creationTime;
        this.timeStamp = existing.timeStamp;
        this.rtt = existing.rtt;
        
        this.contactId = existing.contactId;
        this.instanceId = instanceId;
        this.socketAddress = existing.socketAddress;
        this.contactAddress = existing.contactAddress;
        this.remoteAddress = existing.remoteAddress;
        
        this.type = existing.type;
    }
    
    protected Contact(Contact existing, long rtt, TimeUnit unit) {
        this.creationTime = existing.creationTime;
        this.timeStamp = existing.timeStamp;
        this.rtt = unit.toMillis(rtt);
        
        this.contactId = existing.contactId;
        this.instanceId = existing.instanceId;
        this.socketAddress = existing.socketAddress;
        this.contactAddress = existing.contactAddress;
        this.remoteAddress = existing.remoteAddress;
        
        this.type = existing.type;
    }
    
    protected Contact(Contact existing, 
            SocketAddress socketAddress, 
            SocketAddress contactAddress) {
        
        this.creationTime = existing.creationTime;
        this.timeStamp = existing.timeStamp;
        this.rtt = existing.rtt;
        
        this.contactId = existing.contactId;
        this.instanceId = existing.instanceId;
        this.socketAddress = Arguments.notNull(socketAddress, "socketAddress");
        this.contactAddress = Arguments.notNull(contactAddress, "contactAddress");
        this.remoteAddress = combine(socketAddress, contactAddress);
        
        this.type = existing.type;
    }
    
    /**
     * 
     */
    protected Contact(Contact existing, Contact other) {
        
        if (!existing.equals(other)) {
            throw new IllegalArgumentException(existing + " vs. " + other);
        }
        
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
        
        this.contactId = existing.contactId;
        
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
    
    /**
     * Returns the amount of time in the given {@link TimeUnit} that 
     * has passed since we had contact with this {@link Contact}.
     */
    public long getTimeSinceLastContact(TimeUnit unit) {
        long time = System.currentTimeMillis() - timeStamp;
        return unit.convert(time, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Returns the amount of time in milliseconds that has passed since 
     * we had contact with this {@link Contact}.
     */
    public long getTimeSinceLastContactInMillis() {
        return getTimeSinceLastContact(TimeUnit.MILLISECONDS);
    }
    
    /**
     * Returns {@code true} if we haven't had any contact with this
     * {@link Contact} for the given period of time.
     * 
     * @see #getTimeSinceLastContact(TimeUnit)
     * @see #getTimeSinceLastContactInMillis()
     */
    public boolean isTimeout(long timeout, TimeUnit unit) {
        return getTimeSinceLastContact(unit) >= timeout;
    }
    
    /**
     * Returns the adaptive timeout for this {@link Contact}.
     */
    public long getAdaptiveTimeout(double multiplier, 
            long defaultTimeout, TimeUnit unit) {
        
        long rttInMillis = getRoundTripTimeInMillis();
        if (0L < rttInMillis && 0d < multiplier) {
            long timeout = (long)(rttInMillis * multiplier);
            long adaptive = Math.min(timeout, 
                    unit.toMillis(defaultTimeout));
            return unit.convert(adaptive, TimeUnit.MILLISECONDS);
        }
        
        return defaultTimeout;
    }
    
    @Override
    public KUID getId() {
        return contactId;
    }
    
    /**
     * Returns the {@link Contact}'s instance ID
     */
    public int getInstanceId() {
        return instanceId;
    }
    
    /**
     * Sets the {@link Contact}'s instance ID and returns a new {@link Contact}.
     */
    public Contact setInstanceId(int instanceId) {
        return this.instanceId != instanceId ? new Contact(this, instanceId) : this;
    }
    
    /**
     * Returns the {@link Contact}'s address as reported by 
     * the {@link Socket}.
     */
    public SocketAddress getSocketAddress() {
        return socketAddress;
    }
    
    /**
     * Sets the {@link Contact}'s address as reported by the {@link Socket}.
     */
    public Contact setSocketAddress(SocketAddress address) {
        return new Contact(this, address, contactAddress);
    }
    
    /**
     * Returns the {@link Contact}'s address as reported by 
     * the remote {@link Contact}.
     */
    public SocketAddress getContactAddress() {
        return contactAddress;
    }
    
    /**
     * Sets the {@link Contact}'s address as reported by the 
     * remote {@link Contact}.
     */
    public Contact setContactAddress(SocketAddress address) {
        return new Contact(this, socketAddress, address);
    }
    
    /**
     * Returns the {@link Contact}'s remove address.
     */
    public SocketAddress getRemoteAddress() {
        return remoteAddress;
    }
    
    /**
     * Returns the {@link Type} of the {@link Contact}
     */
    public Type getType() {
        return type;
    }
    
    /**
     * Returns {@code true} if the {@link Contact} is of the given {@link Type}
     */
    public boolean isType(Type type) {
        return type == this.type;
    }
    
    /**
     * Returns {@code true} if this is a an authoritative {@link Contact}.
     */
    public boolean isAuthoritative() {
        return isType(Type.AUTHORITATIVE);
    }
    
    /**
     * Returns {@code true} if this {@link Contact} was discovered 
     * through solicited communication.
     */
    public boolean isSolicited() {
        return isType(Type.SOLICITED);
    }
    
    /**
     * Returns {@code true} if this {@link Contact} was discovered 
     * through unsolicited communication.
     */
    public boolean isUnsolicited() {
        return isType(Type.UNSOLICITED);
    }
    
    /**
     * Returns true if the {@link Contact} is considered active.
     * 
     * @see Type
     */
    public boolean isActive() {
        return type.isActive();
    }
    
    /**
     * Changes the {@link Contact}'s Round-Trip-Time (RTT)
     */
    public Contact setRoundTripTime(long rtt, TimeUnit unit) {
        return new Contact(this, rtt, unit);
    }
    
    /**
     * Returns the {@link Contact}'s Round-Trip-Time (RTT) or a negative 
     * value if the RTT is unknown.
     */
    public long getRoundTripTime(TimeUnit unit) {
        return unit.convert(rtt, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Returns the {@link Contact}'s Round-Trip-Time (RTT) in milliseconds
     * or a negative value if the RTT is unknown.
     */
    public long getRoundTripTimeInMillis() {
        return getRoundTripTime(TimeUnit.MILLISECONDS);
    }
    
    /**
     * Merges this with the other {@link Contact}.
     */
    public Contact merge(Contact other) {
        return other != this ? new Contact(this, other) : this;
    }
    
    @Override
    public int hashCode() {
        return contactId.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof Contact)) {
            return false;
        }
        
        Contact other = (Contact)o;
        return contactId.equals(other.contactId);
    }
    
    @Override
    public int compareTo(Contact o) {
        return contactId.compareTo(o.contactId);
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
        InetAddress address = NetworkUtils.getAddress(socketAddress);
        int port = NetworkUtils.getPort(contactAddress);
        return new InetSocketAddress(address, port);
    }
}
