package com.ardverk.dht.routing;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.KUID;
import com.ardverk.dht.lang.Identifier;

abstract class AbstractContact implements IContact {
    
    private static final long serialVersionUID = 9018341814707545676L;
    
    protected final KUID contactId;
    
    public AbstractContact(Identifier identifier) {
        this.contactId = identifier.getId();
    }
    
    @Override
    public KUID getId() {
        return contactId;
    }
    
    @Override
    public boolean isType(Type type) {
        return type == getType();
    }
    
    @Override
    public boolean isAuthoritative() {
        return isType(Type.AUTHORITATIVE);
    }
    
    @Override
    public boolean isSolicited() {
        return isType(Type.SOLICITED);
    }
    
    @Override
    public boolean isUnsolicited() {
        return isType(Type.UNSOLICITED);
    }
    
    @Override
    public boolean isActive() {
        return getType().isActive();
    }
    
    @Override
    public long getRoundTripTimeInMillis() {
        return getRoundTripTime(TimeUnit.MILLISECONDS);
    }
    
    @Override
    public long getTimeSinceLastContactInMillis() {
        return getTimeSinceLastContact(TimeUnit.MILLISECONDS);
    }
    
    @Override
    public boolean isTimeout(long timeout, TimeUnit unit) {
        return getTimeSinceLastContact(unit) >= timeout;
    }
    
    @Override
    public long getAdaptiveTimeout(double multiplier, 
            long defaultTimeout, TimeUnit unit) {
        
        long rttInMillis = getRoundTripTimeInMillis();
        if (0L < rttInMillis && 0d < multiplier) {
            long timeout = (long)(rttInMillis * multiplier);
            long adaptive = Math.min(timeout, 
                    unit.toMillis(defaultTimeout));
            return unit.convert(adaptive, TimeUnit.MILLISECONDS);
        }
        
        return defaultTimeout;
    }
    
    @Override
    public int hashCode() {
        return contactId.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof IContact)) {
            return false;
        }
        
        IContact other = (IContact)o;
        return contactId.equals(other.getId());
    }
    
    @Override
    public int compareTo(IContact o) {
        return contactId.compareTo(o.getId());
    }
}
