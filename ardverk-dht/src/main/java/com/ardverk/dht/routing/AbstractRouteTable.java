package com.ardverk.dht.routing;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.ardverk.lang.NullArgumentException;

import com.ardverk.dht.ContactPinger;
import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.concurrent.ArdverkValueFuture;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.utils.EventUtils;

public abstract class AbstractRouteTable implements RouteTable {
    
    private final AtomicReference<ContactPinger> pingerRef 
        = new AtomicReference<ContactPinger>();
    
    private final List<RouteTableListener> listeners 
        = new CopyOnWriteArrayList<RouteTableListener>();
    
    @Override
    public void bind(ContactPinger pinger) {
        if (!pingerRef.compareAndSet(null, pinger)) {
            throw new IllegalStateException();
        }
    }


    @Override
    public void unbind() {
        pingerRef.set(null);
    }
    
    @Override
    public boolean isBound() {
        return pingerRef.get() != null;
    }

    protected ArdverkFuture<PingEntity> ping(Contact contact, 
            long timeout, TimeUnit unit) {
        ContactPinger pinger = pingerRef.get();
        
        ArdverkFuture<PingEntity> future = null;
        if (pinger != null) {
            future = pinger.ping(contact, timeout, unit);
        }
        
        if (future != null) {
            return future;
        }
        
        IllegalStateException exception 
            = new IllegalStateException();
        
        return new ArdverkValueFuture<PingEntity>(exception);
    }
    
    @Override
    public void addRouteTableListener(RouteTableListener l) {
        if (l == null) {
            throw new NullArgumentException("l");
        }
        
        listeners.add(l);
    }

    @Override
    public void removeRouteTableListener(RouteTableListener l) {
        if (l == null) {
            throw new NullArgumentException("l");
        }
        
        listeners.remove(l);
    }
    
    @Override
    public RouteTableListener[] getRouteTableListeners() {
        return listeners.toArray(new RouteTableListener[0]);
    }

    protected void fireSplitBucket(Bucket bucket, Bucket left, Bucket right) {
        Runnable event = new Runnable() {
            @Override
            public void run() {
                for (RouteTableListener l : listeners) {
                    
                }
            }
        };
        
        EventUtils.fireEvent(event);
    }
    
    protected void fireContactAdded(Bucket bucket, Contact contact) {
        Runnable event = new Runnable() {
            @Override
            public void run() {
                for (RouteTableListener l : listeners) {
                    
                }
            }
        };
        
        EventUtils.fireEvent(event);
    }
    
    protected void fireContactReplaced(Bucket bucket, 
            Contact existing, Contact contact) {
        Runnable event = new Runnable() {
            @Override
            public void run() {
                for (RouteTableListener l : listeners) {
                    
                }
            }
        };
        
        EventUtils.fireEvent(event);
    }
    
    protected void fireContactChanged(Bucket bucket, 
            Contact existing, Contact contact) {
        Runnable event = new Runnable() {
            @Override
            public void run() {
                for (RouteTableListener l : listeners) {
                    
                }
            }
        };
        
        EventUtils.fireEvent(event);
    }
    
    protected void fireCollision(Contact contact) {
        Runnable event = new Runnable() {
            @Override
            public void run() {
                for (RouteTableListener l : listeners) {
                    
                }
            }
        };
        
        EventUtils.fireEvent(event);
    }
    
    protected void fireReplaceContact(Bucket bucket, 
            Contact existing, Contact contact) {
        Runnable event = new Runnable() {
            @Override
            public void run() {
                for (RouteTableListener l : listeners) {
                    
                }
            }
        };
        
        EventUtils.fireEvent(event);
    }
}
