package com.ardverk.dht.concurrent;

import java.util.concurrent.RunnableFuture;

public interface ArdverkRunnableFuture<V> extends ArdverkFuture<V>, RunnableFuture<V> {

}
