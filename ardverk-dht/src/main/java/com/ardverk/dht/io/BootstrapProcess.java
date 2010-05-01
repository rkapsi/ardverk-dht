package com.ardverk.dht.io;

import org.ardverk.concurrent.AsyncProcess;
import org.ardverk.concurrent.AsyncProcessFuture;

import com.ardverk.dht.entity.BootstrapEntity;

public class BootstrapProcess implements AsyncProcess<BootstrapEntity> {

    private AsyncProcessFuture<BootstrapEntity> future = null;
    
    @Override
    public void start(AsyncProcessFuture<BootstrapEntity> future) {
        this.future = future;
        start();
    }
    
    private void start() {
        
    }
}
