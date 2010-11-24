package com.ardverk.dht.routing;

import org.ardverk.lang.NullArgumentException;
import org.ardverk.net.NetworkUtils;

import com.ardverk.dht.KUID;

class ContactEntity implements Identity {
    
    private static final int MAX_ERRORS = 5;
    
    private Contact contact;
    
    private int errorCount = 0;
    
    private long errorTimeStamp;
    
    public ContactEntity(Contact contact) {
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
        if (!getId().equals(other.getId())) {
            throw new IllegalArgumentException();
        }
        
        if (isSolicited() && other.isUnsolicited()) {
            throw new IllegalArgumentException();
        }
        
        Contact previous = contact;
        contact = previous.merge(other);
        
        if (other.isActive()) {
            errorCount = 0;
            errorTimeStamp = 0;
        }
        
        return new Update(previous, other, contact);
    }
    
    public Contact replaceContact(Contact contact) {
        return update(contact).getPrevious();
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
        return errorCount >= MAX_ERRORS;
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
    
    private static final long X = 5L*60L*1000L;
    
    public boolean hasBeenActiveRecently() {
        return (System.currentTimeMillis() - getTimeStamp()) < X;
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

        public Update(Contact previous, Contact other, Contact merged) {
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