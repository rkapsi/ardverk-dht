package com.ardverk.dht;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncProcessFuture;

import com.ardverk.dht.entity.PingEntity;

abstract class AbstractDHT implements DHT {

    @Override
    public AsyncProcessFuture<PingEntity> ping(String address, int port, 
            long timeout, TimeUnit unit) {
        return ping(new InetSocketAddress(address, port), timeout, unit);
    }
    
    @Override
    public AsyncProcessFuture<PingEntity> ping(InetAddress address, int port, 
            long timeout, TimeUnit unit) {
        return ping(new InetSocketAddress(address, port), timeout, unit);
    }
}
