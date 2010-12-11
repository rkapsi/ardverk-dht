package com.ardverk.dht;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncFutureListener;
import org.ardverk.concurrent.FutureUtils;

import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.concurrent.ArdverkValueFuture;
import com.ardverk.dht.config.LookupConfig;
import com.ardverk.dht.config.PingConfig;
import com.ardverk.dht.config.RefreshConfig;
import com.ardverk.dht.entity.DefaultRefreshEntity;
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.entity.RefreshEntity;
import com.ardverk.dht.routing.Bucket;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.RouteTable;
import com.ardverk.dht.utils.IdentifierUtils;

public class RouteTableManager {

    private final DHT dht;
    
    private final RouteTable routeTable;
    
    RouteTableManager(DHT dht, RouteTable routeTable) {
        this.dht = dht;
        this.routeTable = routeTable;
    }
    
    @SuppressWarnings("unchecked")
    public ArdverkFuture<RefreshEntity> refresh(RefreshConfig config) {
        
        long startTime = System.currentTimeMillis();
        
        List<ArdverkFuture<PingEntity>> pingFutures 
            = new ArrayList<ArdverkFuture<PingEntity>>();
        
        List<ArdverkFuture<NodeEntity>> lookupFutures 
            = new ArrayList<ArdverkFuture<NodeEntity>>();
        
        synchronized (routeTable) {
            int pingCount = (int)(routeTable.getK() * config.getPingCount());
            
            Contact localhost = dht.getLocalhost();
            KUID localhostId = localhost.getId();

            if (0 < pingCount) {
                PingConfig pingConfig = config.getPingConfig();
                long contactTimeout = config.getContactTimeoutInMillis();
                
                Contact[] contacts = routeTable.select(localhostId, pingCount);
                for (Contact contact : contacts) {
                    // Don't send PINGs to the localhost!
                    if (contact.equals(localhost)) {
                        continue;
                    }
                    
                    if (contact.isTimeout(contactTimeout, TimeUnit.MILLISECONDS)) {
                        ArdverkFuture<PingEntity> future 
                            = dht.ping(contact, pingConfig);
                        pingFutures.add(future);
                    }
                }
            }
            
            LookupConfig lookupConfig = config.getLookupConfig();
            long bucketTimeout = config.getBucketTimeoutInMillis();
            
            Bucket[] buckets = routeTable.getBuckets();
            IdentifierUtils.byXor(buckets, localhostId);
            
            for (Bucket bucket : buckets) {
                if (bucket.contains(localhostId)) {
                    continue;
                }
                
                long timeStamp = bucket.getTimeStamp();
                if ((System.currentTimeMillis() - timeStamp) < bucketTimeout) {
                    continue;
                }
                
                // Select a random ID with this prefix
                KUID randomId = KUID.createWithPrefix(
                        bucket.getId(), bucket.getDepth());
                
                ArdverkFuture<NodeEntity> future 
                    = dht.lookup(randomId, lookupConfig);
                lookupFutures.add(future);
            }
        }
        
        ArdverkFuture<PingEntity>[] pings 
            = pingFutures.toArray(new ArdverkFuture[0]);
        
        ArdverkFuture<NodeEntity>[] lookups 
            = lookupFutures.toArray(new ArdverkFuture[0]);
        
        return new RefreshFuture(startTime, pings, lookups);
    }
    
    public static class RefreshFuture extends ArdverkValueFuture<RefreshEntity> {
        
        private final AtomicInteger countdown = new AtomicInteger();
        
        private final long startTime;
        
        private final ArdverkFuture<PingEntity>[] pingFutures;
        
        private final ArdverkFuture<NodeEntity>[] lookupFutures;
        
        @SuppressWarnings("unchecked")
        private RefreshFuture(long startTime, 
                ArdverkFuture<PingEntity>[] pingFutures, 
                ArdverkFuture<NodeEntity>[] lookupFutures) {
            this.startTime = startTime;
            this.pingFutures = pingFutures;
            this.lookupFutures = lookupFutures;
            
            countdown.set(pingFutures.length + lookupFutures.length);
            
            // It's possible that countdown is 0!
            if (0 < countdown.get()) {
                AsyncFutureListener<?> listener 
                        = new AsyncFutureListener<Object>() {
                    @Override
                    public void operationComplete(AsyncFuture<Object> future) {
                        coutdown();
                    }
                };
                
                for (ArdverkFuture<PingEntity> future : pingFutures) {
                    future.addAsyncFutureListener(
                            (AsyncFutureListener<PingEntity>)listener);
                }
                
                for (ArdverkFuture<NodeEntity> future : lookupFutures) {
                    future.addAsyncFutureListener(
                            (AsyncFutureListener<NodeEntity>)listener);
                }
            } else {
                complete();
            }
        }
        
        public ArdverkFuture<PingEntity>[] getPingFutures() {
            return pingFutures;
        }

        public ArdverkFuture<NodeEntity>[] getLookupFutures() {
            return lookupFutures;
        }
        
        @Override
        protected void done() {
            super.done();
            
            FutureUtils.cancelAll(pingFutures, true);
            FutureUtils.cancelAll(lookupFutures, true);
        }
        
        private void coutdown() {
            if (countdown.decrementAndGet() == 0) {
                complete();
            }
        }
        
        private void complete() {
            long time = System.currentTimeMillis() - startTime;
            setValue(new DefaultRefreshEntity(pingFutures, lookupFutures, 
                    time, TimeUnit.MILLISECONDS));
        }
    }
}
