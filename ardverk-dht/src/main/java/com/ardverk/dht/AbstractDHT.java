package com.ardverk.dht;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.ardverk.concurrent.AsyncFuture;

abstract class AbstractDHT implements DHT {

    @Override
    public AsyncFuture<Pong> ping(String address, int port) {
        return ping(new InetSocketAddress(address, port));
    }
    
    @Override
    public AsyncFuture<Pong> ping(InetAddress address, int port) {
        return ping(new InetSocketAddress(address, port));
    }
}
