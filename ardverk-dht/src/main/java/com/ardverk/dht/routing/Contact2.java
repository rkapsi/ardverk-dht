package com.ardverk.dht.routing;

import java.io.Serializable;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.ardverk.collection.CollectionsUtils;
import org.ardverk.lang.NullArgumentException;
import org.ardverk.lang.NumberUtils;

import com.ardverk.dht.KUID;

/**
 * 
 */
public class Contact2 implements Comparable<Contact2>, Cloneable, Serializable {
    
    private static final long serialVersionUID = 298059770472298142L;

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
        SOLICITED(2, true);
        
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
    
    private final long creationTime;
    
    private final long timeStamp;
    
    private final long rtt;
    
    private final Type type;
    
    private final KUID contactId;
    
    private final int instanceId;
    
    private final SocketAddress socketAddress;
    
    private final SocketAddress contactAddress;
    
    private final Map<?, ?> attributes;
    
    /**
     * Creates a {@link Contact2}
     */
    public Contact2(Type type, 
            KUID contactId, 
            int instanceId, 
            SocketAddress address) {
        this(type, contactId, instanceId, address, address, 
                null, -1L, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Creates a {@link Contact2}
     */
    public Contact2(Type type, 
            KUID contactId, 
            int instanceId, 
            SocketAddress address, 
            Map<?, ?> attributes) {
        this(type, contactId, instanceId, address, address, 
                attributes, -1L, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Creates a {@link Contact2}
     */
    public Contact2(Type type, 
            KUID contactId, 
            int instanceId, 
            SocketAddress address, 
            Map<?, ?> attributes, 
            long rtt, TimeUnit unit) {
        this(type, contactId, instanceId, address, 
                address, attributes, rtt, unit);
    }
    
    /**
     * Creates a {@link Contact2}
     */
    public Contact2(Type type, 
            KUID contactId, 
            int instanceId, 
            SocketAddress socketAddress, 
            SocketAddress contactAddress) {
        this(type, contactId, instanceId, socketAddress, 
                contactAddress, null, -1L, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Creates a {@link Contact2}
     */
    public Contact2(Type type, 
            KUID contactId, 
            int instanceId, 
            SocketAddress socketAddress, 
            SocketAddress contactAddress,
            Map<?, ?> attributes) {
        this(type, contactId, instanceId, socketAddress, 
                contactAddress, attributes, -1L, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Creates a {@link Contact2}
     */
    public Contact2(Type type, 
            KUID contactId, 
            int instanceId, 
            SocketAddress socketAddress, 
            SocketAddress contactAddress,
            Map<?, ?> attributes,
            long rtt, TimeUnit unit) {
        
        
        if (type == null) {
            throw new NullArgumentException("type");
        }
        
        if (contactId == null) {
            throw new NullArgumentException("contactId");
        }
        
        if (socketAddress == null) {
            throw new NullArgumentException("socketAddress");
        }
        
        if (contactAddress == null) {
            contactAddress = socketAddress;
        }
        
        this.type = type;
        this.creationTime = System.currentTimeMillis();
        this.timeStamp = creationTime;
        this.rtt = unit.toMillis(rtt);
        
        this.contactId = contactId;
        this.instanceId = instanceId;
        this.socketAddress = socketAddress;
        this.contactAddress = contactAddress;
        
        this.attributes = CollectionsUtils.copy(attributes);
    }
    
    /**
     * 
     */
    protected Contact2(Contact2 existing, Map<?, ?> attributes) {
        if (existing == null) {
            throw new NullArgumentException("existing");
        }
        
        if (attributes == null) {
            throw new NullArgumentException("attributes");
        }
        
        this.creationTime = existing.creationTime;
        this.timeStamp = existing.timeStamp;
        this.rtt = existing.rtt;
        
        this.contactId = existing.contactId;
        this.instanceId = existing.instanceId;
        this.socketAddress = existing.socketAddress;
        this.contactAddress = existing.contactAddress;
        this.type = existing.type;
        
        this.attributes = attributes;
    }
    
    /**
     * 
     */
    /*protected Contact2(Contact2 existing, Type type) {
        if (existing == null) {
            throw new NullArgumentException("existing");
        }
        
        if (type == null) {
            throw new NullArgumentException("type");
        }
        
        this.creationTime = existing.creationTime;
        this.timeStamp = existing.timeStamp;
        this.rtt = existing.rtt;
        
        this.contactId = existing.contactId;
        this.instanceId = existing.instanceId;
        this.socketAddress = existing.socketAddress;
        this.contactAddress = existing.contactAddress;
        this.type = type;
        
        this.attributes = existing.attributes;
    }*/
    
    /**
     * 
     */
    /*protected Contact2(Contact2 existing, int instanceId) {
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
        this.type = existing.type;
        
        this.attributes = existing.attributes;
    }*/
    
    /**
     * 
     */
    /*protected Contact2(Contact2 existing, long rtt, TimeUnit unit) {
        if (existing == null) {
            throw new NullArgumentException("existing");
        }
        
        this.creationTime = existing.creationTime;
        this.timeStamp = existing.timeStamp;
        this.rtt = unit.toMillis(rtt);
        
        this.contactId = existing.contactId;
        this.instanceId = existing.instanceId;
        this.socketAddress = existing.socketAddress;
        this.contactAddress = existing.contactAddress;
        this.type = existing.type;
        
        this.attributes = existing.attributes;
    }*/
    
    /**
     * 
     */
    protected Contact2(Contact2 existing, Contact2 other) {
        
        if (existing == null) {
            throw new NullArgumentException("existing");
        }
        
        if (other == null) {
            throw new NullArgumentException("other");
        }
        
        if (!existing.contactId.equals(other.contactId)) {
            throw new IllegalArgumentException();
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
        
        if (existing.isBetterOrEqual(other)) {
            this.instanceId = existing.instanceId;
            this.socketAddress = existing.socketAddress;
            this.contactAddress = existing.contactAddress;
            this.type = existing.type;
        } else {
            this.instanceId = other.instanceId;
            this.socketAddress = other.socketAddress;
            this.contactAddress = other.contactAddress;
            this.type = other.type;
        }
        
        this.attributes = merge(existing.attributes, other.attributes);
    }
    
    /**
     * 
     */
    private boolean isBetterOrEqual(Contact2 other) {
        return type.isBetterOrEqual(other.type);
    }
    
    /**
     * Returns the {@link Contact}'s creation time
     */
    public long getCreationTime() {
        return creationTime;
    }
    
    /**
     * 
     */
    public long getTimeSinceCreation(TimeUnit unit) {
        long time = System.currentTimeMillis() - creationTime;
        return unit.convert(time, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 
     */
    public long getTimeSinceCreationInMillis() {
        return getTimeSinceCreation(TimeUnit.MILLISECONDS);
    }
    
    /**
     * 
     */
    public long getTimeStamp() {
        return timeStamp;
    }
    
    /**
     * 
     */
    public long getTimeSinceLastContact(TimeUnit unit) {
        long time = System.currentTimeMillis() - timeStamp;
        return unit.convert(time, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 
     */
    public long getTimeSinceLastContactInMillis() {
        return getTimeSinceLastContact(TimeUnit.MILLISECONDS);
    }
    
    /**
     * Returns the adaptive timeout for this {@link Contact2}.
     */
    public long getAdaptiveTimeout(long defaultValue, TimeUnit unit) {
        return defaultValue;
    }
    
    /**
     * Returns the {@link Contact2}'s unique {@link KUID}
     */
    public KUID getContactId() {
        return contactId;
    }
    
    /**
     * Returns the {@link Contact2}'s instance ID
     */
    public int getInstanceId() {
        return instanceId;
    }
    
    /**
     * Returns the {@link Contact2}'s address as reported by 
     * the {@link Socket}.
     */
    public SocketAddress getSocketAddress() {
        return socketAddress;
    }
    
    /**
     * Returns the {@link Contact2}'s address as reported by 
     * the {@link Contact2}.
     */
    public SocketAddress getContactAddress() {
        return contactAddress;
    }
    
    /**
     * 
     */
    public SocketAddress getRemoteAddress() {
        return getContactAddress();
    }
    
    /**
     * Returns the {@link Type} of the {@link Contact2}
     */
    public Type getType() {
        return type;
    }
    
    /**
     * Returns true if the {@link Contact2} is of the given {@link Type}
     */
    public boolean isType(Type type) {
        return type == this.type;
    }
    
    /**
     * 
     */
    public boolean isSolicited() {
        return isType(Type.SOLICITED);
    }
    
    /**
     * 
     */
    public boolean isUnsolicited() {
        return isType(Type.UNSOLICITED);
    }
    
    /**
     * Returns true if the {@link Contact2} is considered active.
     * 
     * @see Type
     */
    public boolean isActive() {
        return type.isActive();
    }
    
    /**
     * Changes the {@link Contact2}'s {@link Type}
     */
    /*public Contact2 setType(Type type) {
        return type != this.type ? new Contact2(this, type) : this;
    }*/
    
    /**
     * Changes the {@link Contact2}'s instance ID
     */
    /*public Contact2 setInstanceId(int instanceId) {
        return instanceId != this.instanceId ? new Contact2(this, instanceId) : this;
    }*/
    
    /**
     * Changes the {@link Contact2}'s Round-Trip-Time (RTT)
     */
    /*public Contact2 setRoundTripTime(long time, TimeUnit unit) {
        return new Contact2(this, time, unit);
    }*/
    
    /**
     * Returns the {@link Contact2}'s Round-Trip-Time (RTT) or a negative 
     * value if the RTT is unknown.
     */
    public long getRoundTripTime(TimeUnit unit) {
        return unit.convert(rtt, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Returns the {@link Contact2}'s Round-Trip-Time (RTT) in milliseconds
     * or a negative value if the RTT is unknown.
     */
    public long getRoundTripTimeInMillis() {
        return getRoundTripTime(TimeUnit.MILLISECONDS);
    }
    
    /**
     * 
     */
    public Contact2 merge(Contact2 other) {
        return other != this ? new Contact2(this, other) : this;
    }
    
    /**
     * 
     */
    public Contact2 put(Object key, Object value) {
        if (key == null) {
            throw new NullArgumentException("key");
        }
        
        if (value == null) {
            throw new NullArgumentException("value");
        }
        
        Map<Object, Object> copy 
            = new HashMap<Object, Object>(attributes);
        copy.put(key, value);
        return new Contact2(this, copy);
    }
    
    /**
     * 
     */
    public Contact2 putAll(Map<?, ?> m) {
        if (m == null) {
            throw new NullArgumentException("m");
        }
        
        Map<Object, Object> copy 
            = new HashMap<Object, Object>(attributes);
        
        for (Map.Entry<?, ?> entry : m.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            
            if (key == null) {
                throw new NullArgumentException("key");
            }
            
            if (value == null) {
                throw new NullArgumentException("value");
            }
            
            copy.put(key, value);
        }
        
        return new Contact2(this, copy);
    }
    
    /**
     * 
     */
    public Contact2 remove(Object key) {
        if (key == null) {
            throw new NullArgumentException("key");
        }
        
        // Do it only if the key exists.
        if (containsKey(key)) {
            Map<Object, Object> copy 
                = new HashMap<Object, Object>(attributes);
            copy.remove(key);
            
            // Replace it with an empty Map
            if (copy.isEmpty()) {
                copy = Collections.emptyMap();
            }
            
            return new Contact2(this, copy);
        }
        
        return this;
    }
    
    /**
     * 
     */
    public boolean containsKey(Object key) {
        return attributes.containsKey(key);
    }
    
    /**
     * 
     */
    public Object get(Object key) {
        return attributes.get(key);
    }
    
    /**
     * 
     */
    public Map<Object, Object> getAll() {
        return Collections.unmodifiableMap(attributes);
    }
    
    /**
     * 
     */
    public boolean getBoolean(Object key) {
        return NumberUtils.getBoolean(attributes.get(key));
    }
    
    /**
     * 
     */
    public boolean getBoolean(Object key, boolean defaultValue) {
        return NumberUtils.getBoolean(attributes.get(key), defaultValue);
    }
    
    /**
     * 
     */
    public int getInteger(Object key) {
        return NumberUtils.getInteger(attributes.get(key));
    }
    
    /**
     * 
     */
    public int getInteger(Object key, int defaultValue) {
        return NumberUtils.getInteger(attributes.get(key), defaultValue);
    }
    
    /**
     * 
     */
    public float getFloat(Object key) {
        return NumberUtils.getFloat(attributes.get(key));
    }
    
    /**
     * 
     */
    public float getFloat(Object key, float defaultValue) {
        return NumberUtils.getFloat(attributes.get(key), defaultValue);
    }
    
    /**
     * 
     */
    public double getDouble(Object key) {
        return NumberUtils.getDouble(attributes.get(key));
    }
    
    /**
     * 
     */
    public double getDouble(Object key, double defaultValue) {
        return NumberUtils.getDouble(attributes.get(key), defaultValue);
    }

    @Override
    public Contact2 clone() {
        return this;
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
        return contactId.equals(other.contactId);
    }
    
    @Override
    public int compareTo(Contact2 o) {
        return contactId.compareTo(o.contactId);
    }
    
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        
        buffer.append("Type=").append(type)
            .append(", contactId=").append(contactId)
            .append(", instanceId=").append(instanceId)
            .append(", socketAddress=").append(socketAddress)
            .append(", contactAddress=").append(contactAddress);
        
        return buffer.toString();
    }

    /**
     * 
     */ 
    private static Map<?, ?> merge(Map<?, ?> m1, Map<?, ?> m2) {
        if (m1.isEmpty()) {
            return m2;
        } else if (m2.isEmpty()) {
            return m1;
        }
        
        Map<Object, Object> copy 
            = new HashMap<Object, Object>(m1);
        copy.putAll(m2);
        
        return copy;
    }
}
