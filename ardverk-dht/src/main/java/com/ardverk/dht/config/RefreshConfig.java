package com.ardverk.dht.config;

import java.util.concurrent.TimeUnit;

public interface RefreshConfig extends Config {

    public float getPingCount();
    
    public void setPingCount(float pingCount);
    
    public long getContactTimeout(TimeUnit unit);
    
    public long getContactTimeoutInMillis();
    
    public void setContactTimeout(long timeout, TimeUnit unit);
    
    public PingConfig getPingConfig();
    
    public void setPingConfig(PingConfig pingConfig);
    
    public LookupConfig getLookupConfig();
    
    public void setLookupConfig(LookupConfig lookupConfig);
    
    public long getBucketTimeout(TimeUnit unit);
    
    public long getBucketTimeoutInMillis();
    
    public void setBucketTimeout(long timeout, TimeUnit unit);
}
