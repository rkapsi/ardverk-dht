package com.ardverk.dht;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.entity.StoreEntity;
import com.ardverk.dht.entity.ValueEntity;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.RouteTable;

public interface DHT2 {
    
    public ArdverkFuture<PingEntity> ping(String host, int port, PingConfig config);
    
    public ArdverkFuture<PingEntity> ping(InetAddress address, int port, PingConfig config);
    
    public ArdverkFuture<PingEntity> ping(SocketAddress address, PingConfig config);
    
    public ArdverkFuture<NodeEntity> lookup(KUID key, LookupConfig config);
    
    public ArdverkFuture<ValueEntity> get(KUID key, ValueConfig config);
    
    public ArdverkFuture<StoreEntity> put(Contact[] dst, KUID key, 
            Value value, StoreConfig config);
    
    
    public static abstract class AbstractDHT implements DHT2 {

        @Override
        public ArdverkFuture<PingEntity> ping(String host, int port, 
                PingConfig config) {
            return ping(new InetSocketAddress(host, port), config);
        }

        @Override
        public ArdverkFuture<PingEntity> ping(InetAddress address, int port,
                PingConfig config) {
            return ping(new InetSocketAddress(address, port), config);
        }
    }
    
    public static interface Value {
        
        public long getContentLength();
        
        public InputStream getContent() throws IOException;
    }
    
    public static class ByteArrayValue implements Value {

        private final byte[] value;
        
        private final int offset;
        
        private final int length;
        
        public ByteArrayValue(byte[] value) {
            this(value, 0, value.length);
        }
        
        public ByteArrayValue(byte[] value, int offset, int length) {
            if (offset < 0 || length < 0 || value.length < (offset+length)) {
                throw new IllegalArgumentException(
                        "offset=" + offset + ", length=" + length);
            }
            
            this.value = value;
            
            this.offset = offset;
            this.length = length;
        }
        
        @Override
        public long getContentLength() {
            return length;
        }

        @Override
        public InputStream getContent() {
            return new ByteArrayInputStream(value, offset, length);
        }
    }
    
    public static interface Config {
        
        public void setTimeout(long timeout, TimeUnit unit);
        
        public long getTimeout(TimeUnit unit);
        
        public long getTimeoutInMillis();
    }
    
    public static class DefaultConfig implements Config {
        
        private volatile long timeoutInMillis;
        
        public DefaultConfig() {
        }
        
        public DefaultConfig(long timeout, TimeUnit unit) {
            setTimeout(timeout, unit);
        }
        
        @Override
        public void setTimeout(long timeout, TimeUnit unit) {
            this.timeoutInMillis = unit.toMillis(timeout);
        }
        
        @Override
        public long getTimeout(TimeUnit unit) {
            return unit.convert(timeoutInMillis, TimeUnit.MILLISECONDS);
        }
        
        @Override
        public long getTimeoutInMillis() {
            return getTimeout(TimeUnit.MILLISECONDS);
        }
    }
    
    public static interface ContactAddress {
        
        public KUID getContactId();
        
        public SocketAddress getContactAddress();
    }
    
    public static class DefaultContactAddress implements ContactAddress {

        private final KUID contactId;
        
        private final SocketAddress address;
        
        public DefaultContactAddress(SocketAddress address) {
            this(null, address);
        }
        
        public DefaultContactAddress(KUID contactId, SocketAddress address) {
            this.contactId = contactId;
            this.address = address;
        }
        
        @Override
        public KUID getContactId() {
            return contactId;
        }

        @Override
        public SocketAddress getContactAddress() {
            return address;
        }
    }
    
    public static interface PingConfig extends Config {
        
    }
    
    public static class DefaultPingConfig extends DefaultConfig 
            implements PingConfig {
        
    }
    
    public static interface LookupConfig extends Config {
        
        public KUID getLookupId();
        
        public Contact[] getContacts();
    }
    
    public static class DefaultLookupConfig extends DefaultConfig 
            implements LookupConfig {

        private final KUID lookupId;
        
        private final Contact[] contacts;
        
        public DefaultLookupConfig(KUID lookupId, Contact[] contacts) {
            this.lookupId = lookupId;
            this.contacts = contacts;
        }
        
        public DefaultLookupConfig(KUID lookupId, RouteTable routeTable) {
            this.lookupId = lookupId;
            this.contacts = routeTable.select(lookupId);
        }
        
        @Override
        public KUID getLookupId() {
            return lookupId;
        }

        @Override
        public Contact[] getContacts() {
            return contacts;
        }
    }
    
    public static interface ValueConfig extends LookupConfig {
        
    }
    
    public static class DefaultValueConfig extends DefaultLookupConfig 
            implements ValueConfig {

        public DefaultValueConfig(KUID lookupId, Contact[] contacts) {
            super(lookupId, contacts);
        }

        public DefaultValueConfig(KUID lookupId, RouteTable routeTable) {
            super(lookupId, routeTable);
        }
    }
    
    public static interface StoreConfig extends Config {
        
    }
    
    public static class DefaultStoreConfig extends DefaultConfig 
            implements StoreConfig {
        
    }
}
