package com.ardverk.dht.io;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncFutureListener;
import org.ardverk.concurrent.AsyncProcess;
import org.ardverk.concurrent.AsyncProcessFuture;
import org.ardverk.lang.NullArgumentException;

import com.ardverk.dht.DHT;
import com.ardverk.dht.KUID;
import com.ardverk.dht.entity.BootstrapEntity;
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.routing.Contact;

public class BootstrapProcess implements AsyncProcess<BootstrapEntity> {

    private final DHT dht;
    
    private final Config config;
    
    private final long timeout;
    
    private final TimeUnit unit;
    
    private volatile long startTime = 0L;
    
    private volatile AsyncFuture<PingEntity> pingFuture = null;
    
    private volatile AsyncFuture<NodeEntity> nodeFuture = null;
    
    private volatile AsyncFuture<BootstrapEntity> future = null;
    
    public BootstrapProcess(DHT dht, Config config, 
            long timeout, TimeUnit unit) {
        
        if (dht == null) {
            throw new NullArgumentException("dht");
        }
        
        if (config == null) {
            throw new NullArgumentException("config");
        }
        
        if (unit == null) {
            throw new NullArgumentException("unit");
        }
        
        this.dht = dht;
        this.config = config;
        this.timeout = timeout;
        this.unit = unit;
    }
    
    @Override
    public void start(AsyncProcessFuture<BootstrapEntity> future) {
        this.future = future;
        
        future.addAsyncFutureListener(new AsyncFutureListener<BootstrapEntity>() {
            @Override
            public void operationComplete(AsyncFuture<BootstrapEntity> future) {
                done();
            }
        });
        
        startTime = System.currentTimeMillis();
        start();
    }
    
    private void done() {
        if (pingFuture != null) {
            pingFuture.cancel(true);
        }
        
        if (nodeFuture != null) {
            nodeFuture.cancel(true);
        }
    }
    
    private void doCancel() {
        future.cancel(true);
    }
    
    private void onException(Throwable t) {
        future.setException(t);
    }
    
    private void doComplete() {
        long time = System.currentTimeMillis() - startTime;
        //future.setValue(new DefaultBootstrapEntity(time, TimeUnit.MILLISECONDS));
        future.setException(new IllegalStateException());
    }
    
    private void start() {
        SocketAddress address = config.getAddress();
        if (address != null) {
            doPing(address);
        } else {
            doLookup(config.getContact());
        }
    }
    
    private void doPing(SocketAddress address) {
        synchronized (future) {
            if (future.isDone()) {
                return;
            }
            
            long timeout = config.getPingTimeoutInMillis();
            pingFuture = dht.ping(address, timeout, TimeUnit.MILLISECONDS);
            
            pingFuture.addAsyncFutureListener(new AsyncFutureListener<PingEntity>() {
                @Override
                public void operationComplete(AsyncFuture<PingEntity> future) {
                    onPong(future);
                }
            });
        }
    }
    
    private void onPong(AsyncFuture<PingEntity> event) {
        synchronized (future) {
            if (future.isDone()) {
                return;
            }
            
            try {
                if (event.isCancelled()) {
                    doCancel();
                } else {
                    doLookup(event.get());
                }
            } catch (Throwable t) {
                onException(t);
            }
        }
    }
    
    private void doLookup(PingEntity entity) {
        doLookup(entity.getContact());
    }
    
    private void doLookup(Contact contact) {
        synchronized (future) {
            if (future.isDone()) {
                return;
            }
            
            Contact localhost = dht.getLocalhost();
            KUID contactId = localhost.getId();
            
            long timeout = config.getLookupTimeoutInMillis();
            nodeFuture = dht.lookup(contactId, timeout, unit);
            
            nodeFuture.addAsyncFutureListener(new AsyncFutureListener<NodeEntity>() {
                @Override
                public void operationComplete(AsyncFuture<NodeEntity> future) {
                    onLookup(future);
                }
            });
        }
    }
    
    private void onLookup(AsyncFuture<NodeEntity> event) {
        synchronized (future) {
            if (future.isDone()) {
                return;
            }
        
            try {
                if (event.isCancelled()) {
                    doCancel();
                } else {
                    doRefresh(event.get());
                }
            } catch (Throwable t) {
                onException(t);
            }
        }
    }
    
    private void doRefresh(NodeEntity entity) {
        synchronized (future) {
            if (future.isDone()) {
                
            }
            
            doComplete();
        }
    }
    
    public static class Config {
        
        private static final long PING_TIMEOUT = 10L * 1000L;
        
        private static final long LOOKUP_TIMEOUT = 40L * 1000L;
        
        private final SocketAddress address;
        
        private final Contact contact;
        
        private volatile long pingTimeout = PING_TIMEOUT;
        
        private volatile long lookupTimeout = LOOKUP_TIMEOUT;
        
        public Config(SocketAddress address) {
            this(address, null);
            
            if (address == null) {
                throw new NullArgumentException("address");
            }
        }
        
        public Config(Contact contact) {
            this(null, contact);
            
            if (contact == null) {
                throw new NullArgumentException("contact");
            }
        }
        
        private Config(SocketAddress address, Contact contact) {
            this.address = address;
            this.contact = contact;
        }
        
        public SocketAddress getAddress() {
            return address;
        }
        
        public Contact getContact() {
            return contact;
        }
        
        public long getPingTimeout(TimeUnit unit) {
            return unit.convert(pingTimeout, TimeUnit.MILLISECONDS);
        }
        
        public long getPingTimeoutInMillis() {
            return getPingTimeout(TimeUnit.MILLISECONDS);
        }
        
        public void setPingTimeout(long timeout, TimeUnit unit) {
            this.pingTimeout = unit.toMillis(timeout);
        }
        
        public long getLookupTimeout(TimeUnit unit) {
            return unit.convert(lookupTimeout, TimeUnit.MILLISECONDS);
        }
        
        public long getLookupTimeoutInMillis() {
            return getLookupTimeout(TimeUnit.MILLISECONDS);
        }
        
        public void setLookupTimeout(long timeout, TimeUnit unit) {
            this.lookupTimeout = unit.toMillis(timeout);
        }
    }
}
