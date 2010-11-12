package com.ardverk.dht.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncFutureListener;
import org.ardverk.concurrent.FutureUtils;

import com.ardverk.dht.entity.DefaultRefreshEntity;
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.entity.RefreshEntity;

public class RefreshFuture extends ArdverkValueFuture<RefreshEntity> {
    
    private final long startTime = System.currentTimeMillis();
    
    private final AtomicInteger coutdown = new AtomicInteger();
    
    private final ArdverkFuture<PingEntity>[] pingFutures;
    
    private final ArdverkFuture<NodeEntity>[] lookupFutures;
    
    @SuppressWarnings("unchecked")
    public RefreshFuture(ArdverkFuture<PingEntity>[] pingFutures, 
            ArdverkFuture<NodeEntity>[] lookupFutures) {
        this.pingFutures = pingFutures;
        this.lookupFutures = lookupFutures;
        
        coutdown.set(pingFutures.length + lookupFutures.length);
        
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
        if (coutdown.decrementAndGet() == 0) {
            long time = System.currentTimeMillis() - startTime;
            setValue(new DefaultRefreshEntity(pingFutures, lookupFutures, 
                    time, TimeUnit.MILLISECONDS));
        }
    }
}
