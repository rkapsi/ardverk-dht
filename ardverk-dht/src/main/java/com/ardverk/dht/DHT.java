package com.ardverk.dht;

import java.net.InetAddress;
import java.net.SocketAddress;

import org.ardverk.concurrent.AsyncFuture;

interface DHT {

    public AsyncFuture<Pong> ping(String address, int port);
    
    public AsyncFuture<Pong> ping(InetAddress address, int port);

    public AsyncFuture<Pong> ping(SocketAddress dst);
    
    public AsyncFuture<Object> put(KUID key, byte[] value);
    
    public AsyncFuture<Object> get(KUID key);
    
    public AsyncFuture<Object> lookup(KUID key);
}
