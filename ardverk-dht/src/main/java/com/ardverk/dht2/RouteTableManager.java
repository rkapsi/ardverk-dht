package com.ardverk.dht2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.ardverk.dht.KUID;
import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.RouteTable;

class RouteTableManager {

    private final DHT dht;
    
    private final RouteTable routeTable;
    
    public RouteTableManager(DHT dht, RouteTable routeTable) {
        this.dht = dht;
        this.routeTable = routeTable;
    }
    
    public ArdverkFuture<?>[] refresh(QueueKey queueKey, RefreshConfig config) {
        
        List<ArdverkFuture<?>> futures 
            = new ArrayList<ArdverkFuture<?>>();
        
        synchronized (routeTable) {
            int pingCount = config.getPingCount();
            if (0 < pingCount) {
                PingConfig pingConfig = config.getPingConfig();
                KUID localhost = dht.getLocalhost().getContactId();
                long contactTimeout = config.getContactTimeoutInMillis();
                
                Contact[] contacts = routeTable.select(localhost, pingCount);
                for (Contact contact : contacts) {
                    if (contact.isTimeout(contactTimeout, TimeUnit.MILLISECONDS)) {
                        ArdverkFuture<PingEntity> future = dht.ping(
                                queueKey, contact, pingConfig);
                        futures.add(future);
                    }
                }
            }
            
            LookupConfig lookupConfig = config.getLookupConfig();
            long bucketTimeout = config.getBucketTimeoutInMillis();
            KUID[] bucketIds = routeTable.select(bucketTimeout, TimeUnit.MILLISECONDS);
            
            for (KUID bucketId : bucketIds) {
                ArdverkFuture<NodeEntity> future = dht.lookup(
                        queueKey, bucketId, lookupConfig);
                futures.add(future);
            }
        }
        
        return futures.toArray(new ArdverkFuture[0]);
    }
}
