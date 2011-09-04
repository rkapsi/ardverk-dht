package org.ardverk.dht.routing;

import java.io.Serializable;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.Duration;
import org.ardverk.dht.KUID;
import org.ardverk.dht.lang.Identifier;
import org.ardverk.lang.TimeStamp;

public class Contact2 implements Identifier, Longevity, 
        Comparable<Contact2>, Serializable {
    
    private static final long serialVersionUID = 3215258031597639857L;

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
    
    public static Builder localhost(KUID contactId) {
        return newContact(Type.AUTHORITATIVE, contactId);
    }

    public static Builder newContact(Type type, KUID contactId) {
        TimeStamp now = TimeStamp.now();
        return new Builder(type, contactId, now, now);
    }
    
    protected final Type type;
    
    protected final KUID contactId;
    
    protected final int instanceId;
    
    protected final TimeStamp creationTime;
    
    protected final TimeStamp timeStamp;
    
    protected final SocketAddress socketAddress;
    
    protected final SocketAddress contactAddress;
    
    protected final Duration rtt;
    
    protected final boolean hidden;
    
    public Contact2(Type type, 
            KUID contactId, 
            int instanceId,
            TimeStamp creationTime, 
            TimeStamp timeStamp,
            SocketAddress socketAddress,
            SocketAddress contactAddress,
            Duration rtt,
            boolean hidden) {
        
        this.type = type;
        this.contactId = contactId;
        this.instanceId = instanceId;
        
        this.creationTime = creationTime;
        this.timeStamp = timeStamp;
        
        this.socketAddress = socketAddress;
        this.contactAddress = contactAddress;
        this.rtt = rtt;
        
        this.hidden = hidden;
    }
    
    public Type getType() {
        return type;
    }
    
    @Override
    public KUID getId() {
        return contactId;
    }
    
    public int getInstanceId() {
        return instanceId;
    }

    @Override
    public TimeStamp getCreationTime() {
        return creationTime;
    }

    @Override
    public TimeStamp getTimeStamp() {
        return timeStamp;
    }
    
    public long getRoundTripTime(TimeUnit unit) {
        return rtt.getTime(unit);
    }
    
    public long getRoundTripTimeInMillis() {
        return rtt.getTimeInMillis();
    }

    public boolean isHidden() {
        return hidden;
    }
    
    public boolean isActive() {
        return type.isActive();
    }
    
    public SocketAddress getSocketAddress() {
        return socketAddress;
    }

    public SocketAddress getContactAddress() {
        return contactAddress;
    }
    
    public SocketAddress getRemoteAddress() {
        return getContactAddress();
    }
    
    public long getTimeSinceLastContact(TimeUnit unit) {
        return getTimeStamp().getAge(unit);
    }
    
    public long getTimeSinceLastContactInMillis() {
        return getTimeSinceLastContact(TimeUnit.MILLISECONDS);
    }
    
    public boolean isTimeout(long timeout, TimeUnit unit) {
        return getTimeSinceLastContact(unit) >= timeout;
    }
    
    public Builder newBuilder() {
        Builder builder = new Builder(type, 
                contactId, creationTime, timeStamp);
        
        builder.setInstanceId(instanceId)
            .setSocketAddress(socketAddress)
            .setContactAddress(contactAddress)
            .setRoundTripTime(rtt)
            .setHidden(hidden);
        
        return builder;
    }
    
    private TimeStamp pickCreationTime(Contact2 other) {
        return creationTime.compareTo(other.creationTime) < 0 
                    ? creationTime : other.creationTime;
    }
    
    private TimeStamp pickTimeStamp(Contact2 other) {
        if (other.isActive()) {
            return other.timeStamp;
        }
        return timeStamp;
    }
    
    private Duration pickRTT(Contact2 other) {
        return other.rtt.getTimeInMillis() > 0L ? other.rtt : rtt;
    }
    
    public Builder merge(Contact2 other) {
        if (type.equals(Type.AUTHORITATIVE) 
                && !other.type.equals(Type.AUTHORITATIVE)) {
            throw new IllegalArgumentException();
        }
        
        if (!contactId.equals(other.contactId)) {
            throw new IllegalArgumentException();
        }
        
        Contact2 better = null;
        if (type.isBetterOrEqual(other.type)) {
            better = this;
        } else {
            better = other;
        }
        
        TimeStamp creationTime = pickCreationTime(other);
        TimeStamp timeStamp = pickTimeStamp(other);
        Duration rtt = pickRTT(other);
        
        Builder builder = new Builder(better.type, 
                contactId, creationTime, timeStamp);
        
        builder.setInstanceId(better.instanceId)
            .setSocketAddress(better.socketAddress)
            .setContactAddress(better.contactAddress)
            .setRoundTripTime(rtt)
            .setHidden(better.hidden);
        
        return builder;
    }
    
    @Override
    public int hashCode() {
        return contactId.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof Contact2)) {
            return false;
        }
        
        Contact2 other = (Contact2)o;
        return contactId.equals(other.getId());
    }
    
    @Override
    public int compareTo(Contact2 o) {
        return contactId.compareTo(o.getId());
    }
    
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        
        buffer.append("Type=").append(getType())
            .append(", contactId=").append(getId())
            .append(", instanceId=").append(getInstanceId())
            .append(", socketAddress=").append(getSocketAddress())
            .append(", contactAddress=").append(getContactAddress())
            .append(", rtt=").append(getRoundTripTimeInMillis());
        return buffer.toString();
    }
    
    public static class Builder {
        
        private final Type type;
        
        private final KUID contactId;
        
        private final TimeStamp creationTime;
        
        private final TimeStamp timeStamp;
        
        private int instanceId = 0;
        
        private SocketAddress socketAddress;
        
        private SocketAddress contactAddress;
        
        private Duration rtt = Duration.ZERO;
        
        private boolean hidden = false;
        
        private Builder(Type type, KUID contactId, 
                TimeStamp creationTime, TimeStamp timeStamp) {
            
            this.type = type;
            this.contactId = contactId;
            
            this.creationTime = creationTime;
            this.timeStamp = timeStamp;
        }
        
        public Builder setInstanceId(int instanceId) {
            this.instanceId = instanceId;
            return this;
        }
        
        public Builder setSocketAddress(SocketAddress socketAddress) {
            this.socketAddress = socketAddress;
            return this;
        }
        
        public Builder setContactAddress(SocketAddress contactAddress) {
            this.contactAddress = contactAddress;
            return this;
        }
        
        public Builder setRoundTripTime(long duration, TimeUnit unit) {
            return setRoundTripTime(new Duration(duration, unit));
        }
        
        private Builder setRoundTripTime(Duration rtt) {
            this.rtt = rtt;
            return this;
        }
        
        public Builder setHidden(boolean hidden) {
            this.hidden = hidden;
            return this;
        }
        
        public Contact2 build() {
            return new Contact2(type, contactId, instanceId, 
                    creationTime, timeStamp, 
                    socketAddress, contactAddress, 
                    rtt, hidden);
        }
    }
}
