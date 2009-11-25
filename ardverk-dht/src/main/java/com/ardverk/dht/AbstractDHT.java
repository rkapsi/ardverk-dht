package com.ardverk.dht;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.ardverk.concurrent.AsyncFuture;

import com.ardverk.dht.message.PingResponse;

abstract class AbstractDHT implements DHT {

    @Override
    public AsyncFuture<PingResponse> ping(String address, int port) {
        return ping(new InetSocketAddress(address, port));
    }
    
    @Override
    public AsyncFuture<PingResponse> ping(InetAddress address, int port) {
        return ping(new InetSocketAddress(address, port));
    }
}
