package com.ardverk.dht.routing;

import java.util.concurrent.TimeUnit;

import org.ardverk.net.NetworkMask;

import com.ardverk.dht.config.DefaultPingConfig;
import com.ardverk.dht.config.PingConfig;
import com.ardverk.dht2.Constants;

public class RouteTableSettings {
    
    private final int k;
    
    private volatile PingConfig pingConfig = new DefaultPingConfig();
    
    
    private volatile int maxDepth = Integer.MAX_VALUE;
    
    private volatile int maxCacheSize = 16;
        
    private volatile NetworkMask networkMask = NetworkMask.C;
    
    private volatile double probability = 0.75d;
    
    private volatile int maxConsecutiveErrors = 100;

    private volatile int maxContactsFromSameNetwork = Integer.MAX_VALUE;
    
    private volatile double timeoutMultiplier = Constants.MULTIPLIER;
    
    private volatile long timeoutInMillis = 10L * 1000L;
    
    private volatile int tooManyErrorsCount = Integer.MAX_VALUE;
    
    private volatile boolean checkIdentity = true;
    
    public RouteTableSettings() {
        this(Constants.K);
    }
    
    public RouteTableSettings(int k) {
        this.k = k;
    }
    
    public int getK() {
        return k;
    }
    
    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public int getMaxCacheSize() {
        return maxCacheSize;
    }

    public void setMaxCacheSize(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
    }

    public NetworkMask getNetworkMask() {
        return networkMask;
    }

    public void setNetworkMask(NetworkMask networkMask) {
        this.networkMask = networkMask;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    public int getMaxConsecutiveErrors() {
        return maxConsecutiveErrors;
    }

    public void setMaxConsecutiveErrors(int maxConsecutiveErrors) {
        this.maxConsecutiveErrors = maxConsecutiveErrors;
    }

    public int getMaxContactsFromSameNetwork() {
        return maxContactsFromSameNetwork;
    }

    public void setMaxContactsFromSameNetwork(int maxContactsFromSameNetwork) {
        this.maxContactsFromSameNetwork = maxContactsFromSameNetwork;
    }
    
    public long getTimeout(TimeUnit unit) {
        return unit.convert(timeoutInMillis, TimeUnit.MILLISECONDS);
    }
    
    public long getTimeoutInMillis() {
        return getTimeout(TimeUnit.MILLISECONDS);
    }
    
    public void setTimeout(long timeout, TimeUnit unit) {
        this.timeoutInMillis = unit.toMillis(timeout);
    }

    public int getTooManyErrorsCount() {
        return tooManyErrorsCount;
    }

    public void setTooManyErrorsCount(int tooManyErrorsCount) {
        this.tooManyErrorsCount = tooManyErrorsCount;
    }

    public boolean isCheckIdentity() {
        return checkIdentity;
    }

    public void setCheckIdentity(boolean checkIdentity) {
        this.checkIdentity = checkIdentity;
    }

    public double getTimeoutMultiplier() {
        return timeoutMultiplier;
    }

    public void setTimeoutMultiplier(double timeoutMultiplier) {
        this.timeoutMultiplier = timeoutMultiplier;
    }

    public PingConfig getPingConfig() {
        return pingConfig;
    }

    public void setPingConfig(PingConfig pingConfig) {
        this.pingConfig = pingConfig;
    }
}