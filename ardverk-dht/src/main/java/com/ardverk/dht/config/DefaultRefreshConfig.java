package com.ardverk.dht2;

import java.util.concurrent.TimeUnit;

import org.ardverk.utils.TimeUtils;

public class DefaultRefreshConfig extends AbstractConfig implements RefreshConfig {

    private volatile PingConfig pingConfig = new DefaultPingConfig();

    private volatile int pingCount = Constants.K;
    
    private volatile long contactTimeoutInMillis 
        = TimeUtils.convert(5L, TimeUnit.MINUTES, TimeUnit.MILLISECONDS);
    
    private volatile LookupConfig lookupConfig = new DefaultLookupConfig();
    
    private volatile long bucketTimeoutInMillis 
        = TimeUtils.convert(5L, TimeUnit.MINUTES, TimeUnit.MILLISECONDS);
    
    @Override
    public PingConfig getPingConfig() {
        return pingConfig;
    }
    
    @Override
    public void setPingConfig(PingConfig pingConfig) {
        this.pingConfig = pingConfig;
    }
    
    @Override
    public int getPingCount() {
        return pingCount;
    }
    
    @Override
    public void setPingCount(int pingCount) {
        this.pingCount = pingCount;
    }

    @Override
    public long getContactTimeout(TimeUnit unit) {
        return unit.convert(contactTimeoutInMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public long getContactTimeoutInMillis() {
        return getContactTimeout(TimeUnit.MILLISECONDS);
    }
    
    @Override
    public void setContactTimeout(long timeout, TimeUnit unit) {
        this.contactTimeoutInMillis = unit.toMillis(timeout);
    }

    @Override
    public LookupConfig getLookupConfig() {
        return lookupConfig;
    }
    
    @Override
    public void setLookupConfig(LookupConfig lookupConfig) {
        this.lookupConfig = lookupConfig;
    }

    @Override
    public long getBucketTimeout(TimeUnit unit) {
        return unit.convert(bucketTimeoutInMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public long getBucketTimeoutInMillis() {
        return getBucketTimeout(TimeUnit.MILLISECONDS);
    }
    
    @Override
    public void setBucketTimeout(long timeout, TimeUnit unit) {
        this.bucketTimeoutInMillis = unit.toMillis(timeout);
    }

    @Override
    public void setTimeout(long timeout, TimeUnit unit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getTimeout(TimeUnit unit) {
        throw new UnsupportedOperationException();
    }
}
