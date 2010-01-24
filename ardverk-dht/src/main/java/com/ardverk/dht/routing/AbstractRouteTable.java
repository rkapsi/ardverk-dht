package com.ardverk.dht.routing;

import java.util.concurrent.CopyOnWriteArrayList;

import com.ardverk.dht.ContactPinger;
import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.DefaultRouteTable.Bucket;
import com.ardverk.utils.EventUtils;

public abstract class AbstractRouteTable implements RouteTable {
    
    protected final int k;
    
    protected final ContactPinger pinger;
    
    private final CopyOnWriteArrayList<RouteTableListener> listeners 
        = new CopyOnWriteArrayList<RouteTableListener>();
    
    public AbstractRouteTable(ContactPinger pinger, int k) {
        
        if (pinger == null) {
            throw new NullPointerException("pinger");
        }
        
        if (k <= 0) {
            throw new IllegalArgumentException("k=" + k);
        }
        
        this.pinger = pinger;
        this.k = k;
    }
    
    @Override
    public ContactPinger getContactPinger() {
        return pinger;
    }

    @Override
    public int getK() {
        return k;
    }
    
    @Override
    public Contact[] select(KUID contactId) {
        return select(contactId, getK());
    }

    @Override
    public void addRouteTableListener(RouteTableListener l) {
        if (l == null) {
            throw new NullPointerException("l");
        }
        
        listeners.add(l);
    }

    @Override
    public void removeRouteTableListener(RouteTableListener l) {
        if (l == null) {
            throw new NullPointerException("l");
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
    
    protected void fireContactReplaced(Bucket bucket, Contact existing, Contact contact) {
        Runnable event = new Runnable() {
            @Override
            public void run() {
                for (RouteTableListener l : listeners) {
                    
                }
            }
        };
        
        EventUtils.fireEvent(event);
    }
    
    protected void fireContactChanged(Bucket bucket, Contact existing, Contact contact) {
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
    
    protected void fireReplaceContact(Bucket bucket, Contact existing, Contact contact) {
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
