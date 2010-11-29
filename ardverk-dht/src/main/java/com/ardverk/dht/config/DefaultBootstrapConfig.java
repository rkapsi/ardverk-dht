package com.ardverk.dht.config;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.QueueKey;

public class DefaultBootstrapConfig extends AbstractConfig 
        implements BootstrapConfig {

    private volatile PingConfig pingConfig = new DefaultPingConfig();
    
    private volatile LookupConfig lookupConfig = new DefaultLookupConfig();
    
    @Override
    public QueueKey getQueueKey() {
        return super.getQueueKey();
    }

    @Override
    public void setQueueKey(QueueKey queueKey) {
        super.setQueueKey(queueKey);
        pingConfig.setQueueKey(queueKey);
        lookupConfig.setQueueKey(queueKey);
    }

    @Override
    public PingConfig getPingConfig() {
        return pingConfig;
    }
    
    @Override
    public void setPingConfig(PingConfig pingConfig) {
        this.pingConfig = pingConfig;
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
    public void setOperationTimeout(long timeout, TimeUnit unit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getOperationTimeout(TimeUnit unit) {
        return ConfigUtils.getOperationTimeout(new Config[] { pingConfig, lookupConfig }, unit);
    }
}
