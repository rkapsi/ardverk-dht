package com.ardverk.dht2;

import java.util.concurrent.TimeUnit;

public interface RefreshConfig {

    public int getPingCount();
    
    public long getContactTimeout(TimeUnit unit);
    
    public long getContactTimeoutInMillis();
    
    public PingConfig getPingConfig();
    
    public LookupConfig getLookupConfig();
    
    public long getBucketTimeout(TimeUnit unit);
    
    public long getBucketTimeoutInMillis();
}
