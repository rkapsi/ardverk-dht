package com.ardverk.dht2;

import java.util.concurrent.TimeUnit;

public interface RefreshConfig extends Config {

    public int getPingCount();
    
    public long getContactTimeout(TimeUnit unit);
    
    public long getContactTimeoutInMillis();
    
    public void setContactTimeout(long timeout, TimeUnit unit);
    
    public PingConfig getPingConfig();
    
    public LookupConfig getLookupConfig();
    
    public long getBucketTimeout(TimeUnit unit);
    
    public long getBucketTimeoutInMillis();
    
    public void setBucketTimeout(long timeout, TimeUnit unit);
}
