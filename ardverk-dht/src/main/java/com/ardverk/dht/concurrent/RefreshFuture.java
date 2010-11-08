package com.ardverk.dht.concurrent;

import java.util.concurrent.atomic.AtomicInteger;

import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncFutureListener;
import org.ardverk.concurrent.FutureUtils;

import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.entity.PingEntity;

public class RefreshFuture extends ArdverkValueFuture<Object> {
    
    private final ArdverkFuture<PingEntity>[] pingFutures;
    
    private final ArdverkFuture<NodeEntity>[] lookupFutures;
    
    private final AtomicInteger coutdown = new AtomicInteger();
    
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
            setValue(new Object());
        }
    }
}
