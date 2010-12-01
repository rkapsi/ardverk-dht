package com.ardverk.dht.routing;

import org.ardverk.lang.NullArgumentException;
import org.ardverk.net.NetworkUtils;

import com.ardverk.dht.KUID;
import com.ardverk.dht.lang.Identifier;

class ContactEntity implements Identifier, Longevity {
    
    private final RouteTableConfig config;
    
    private Contact contact;
    
    private int errorCount = 0;
    
    private long errorTimeStamp;
    
    public ContactEntity(RouteTableConfig config, Contact contact) {
        this.config = config;
        this.contact = contact;
    }
    
    @Override
    public long getCreationTime() {
        return contact.getCreationTime();
    }
    
    @Override
    public long getTimeStamp() {
        return contact.getTimeStamp();
    }
    
    @Override
    public KUID getId() {
        return contact.getId();
    }
    
    /**
     * Returns the {@link ContactEntity}'s {@link Contact}.
     */
    public Contact getContact() {
        return contact;
    }
    
    public Update update(Contact other) {
        return update(other, false);
    }
    
    public Contact replace(Contact other) {
        return update(other, true).getPrevious();
    }
    
    private Update update(Contact other, boolean force) {
        Contact previous = contact;
        contact = previous.merge(other);
        
        if (other.isActive()) {
            errorCount = 0;
            errorTimeStamp = 0;
        }
        
        return new Update(previous, other, contact);
    }
    
    public int getErrorCount() {
        return errorCount;
    }
    
    public long getErrorTimeStamp() {
        return errorTimeStamp;
    }
    
    public boolean error() {
        ++errorCount;
        errorTimeStamp = System.currentTimeMillis();
        return isDead();
    }
    
    public boolean isSolicited() {
        return contact.isSolicited();
    }
    
    public boolean isUnsolicited() {
        return contact.isUnsolicited();
    }
    
    public boolean isDead() {
        return errorCount >= config.getMaxContactErrors();
    }
    
    public boolean isAlive() {
        return !isDead() && contact.isActive();
    }
    
    public boolean isUnknown() {
        return !isDead() && isUnsolicited();
    }
    
    public boolean isSameRemoteAddress(Contact contact) {
        return NetworkUtils.isSameAddress(
                this.contact.getRemoteAddress(), 
                contact.getRemoteAddress());
    }
    
    public boolean hasBeenActiveRecently() {
        long timeout = config.getHasBeenActiveTimeoutInMillis();
        return (System.currentTimeMillis() - getTimeStamp()) < timeout;
    }
    
    public boolean same(Contact other) {
        if (other == null) {
            throw new NullArgumentException("other");
        }
        
        return contact != null && contact.equals(other);
    }
    
    public static class Update {
        
        private final Contact previous;
        
        private final Contact other;
        
        private final Contact merged;

        private Update(Contact previous, Contact other, Contact merged) {
            this.previous = previous;
            this.other = other;
            this.merged = merged;
        }

        public Contact getPrevious() {
            return previous;
        }

        public Contact getOther() {
            return other;
        }

        public Contact getMerged() {
            return merged;
        }
    }
}