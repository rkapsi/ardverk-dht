package com.ardverk.dht;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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

    public RouteTable getRouteTable();
    
    public ArdverkFuture<PingEntity> ping(Contact dst, PingConfig config);
    
    public ArdverkFuture<NodeEntity> lookup(KUID key, LookupConfig config);
    
    public ArdverkFuture<StoreEntity> put(Contact[] dst, ValueTuple tuple, StoreConfig config);
    
    public ArdverkFuture<ValueEntity> get(KUID key, ValueConfig config);
    
    public static interface ValueTuple {
        
        public KUID getKey();
        
        public long getContentLength();
        
        public InputStream getContent() throws IOException;
    }
    
    public static class DefaultValueTuple implements ValueTuple {

        private final KUID key;
        
        private final byte[] value;
        
        private final int offset;
        
        private final int length;
        
        public DefaultValueTuple(KUID key, byte[] value) {
            this(key, value, 0, value.length);
        }
        
        public DefaultValueTuple(KUID key, byte[] value, int offset, int length) {
            if (offset < 0 || length < 0 || value.length < (offset+length)) {
                throw new IllegalArgumentException(
                        "offset=" + offset + ", length=" + length);
            }
            
            this.key = key;
            this.value = value;
            
            this.offset = offset;
            this.length = length;
        }
        
        @Override
        public KUID getKey() {
            return key;
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
    
    public static interface ArdverkConfig {
        
        public void setTimeout(long timeout, TimeUnit unit);
        
        public long getTimeout(TimeUnit unit);
        
        public long getTimeoutInMillis();
    }
    
    public static class DefaultArdverkConfig implements ArdverkConfig {
        
        private volatile long timeoutInMillis;
        
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
    
    public static interface PingConfig extends ArdverkConfig {
        
        public void setContactAddress(ContactAddress address);
        
        public ContactAddress getContactAddress();
    }
    
    public static class DefaultPingConfig extends DefaultArdverkConfig 
            implements PingConfig {

        private volatile ContactAddress address;
        
        @Override
        public void setContactAddress(ContactAddress address) {
            this.address = address;
        }

        @Override
        public ContactAddress getContactAddress() {
            return address;
        }
    }
    
    public static interface LookupConfig extends ArdverkConfig {
        
        public KUID getLookupId();
        
        public Contact[] getContacts();
    }
    
    public static class DefaultLookupConfig extends DefaultArdverkConfig 
            implements LookupConfig {

        private final KUID contactId;
        
        private final Contact[] contacts;
        
        public DefaultLookupConfig(KUID contactId, Contact[] contacts) {
            this.contactId = contactId;
            this.contacts = contacts;
        }
        
        public DefaultLookupConfig(KUID contactId, RouteTable routeTable) {
            this.contactId = contactId;
            this.contacts = routeTable.select(contactId);
        }
        
        @Override
        public KUID getLookupId() {
            return contactId;
        }

        @Override
        public Contact[] getContacts() {
            return contacts;
        }
    }
    
    public static interface ValueConfig extends ArdverkConfig {
        
    }
    
    public static interface StoreConfig extends ArdverkConfig {
        
    }
}
