package com.ardverk.dht.config;

import java.util.concurrent.TimeUnit;

public interface RefreshConfig extends Config {

    /**
     * Returns the {@link PingConfig} that's used for refreshing.
     */
    public PingConfig getPingConfig();
    
    /**
     * Sets the {@link PingConfig} that's used for refreshing.
     */
    public void setPingConfig(PingConfig pingConfig);
    
    /**
     * Returns the {@link LookupConfig} that's used for refreshing.
     */
    public LookupConfig getLookupConfig();
    
    /**
     * Returns the {@link LookupConfig} that's used for refreshing.
     */
    public void setLookupConfig(LookupConfig lookupConfig);
    
    public float getPingCount();
    
    public void setPingCount(float pingCount);
    
    public long getContactTimeout(TimeUnit unit);
    
    public long getContactTimeoutInMillis();
    
    public void setContactTimeout(long timeout, TimeUnit unit);
    
    public long getBucketTimeout(TimeUnit unit);
    
    public long getBucketTimeoutInMillis();
    
    public void setBucketTimeout(long timeout, TimeUnit unit);
}
