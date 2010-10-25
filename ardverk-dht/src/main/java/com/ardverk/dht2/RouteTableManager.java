package com.ardverk.dht2;

import com.ardverk.dht.KUID;
import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.routing.RouteTable;

class RouteTableManager {

    private final DHT dht;
    
    private final RouteTable routeTable;
    
    public RouteTableManager(DHT dht, RouteTable routeTable) {
        this.dht = dht;
        this.routeTable = routeTable;
    }
    
    public ArdverkFuture<?> refresh() {
        KUID[] bucketIds = null;
    }
}
