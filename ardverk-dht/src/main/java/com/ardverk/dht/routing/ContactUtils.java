package com.ardverk.dht.routing;

import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import org.ardverk.utils.LongComparator;
import org.ardverk.utils.ReverseComparator;

public class ContactUtils {

    private ContactUtils() {}
      
    private static final Comparator<ContactEntity> HEALTH_ASCENDING 
        = new Comparator<ContactEntity>() {
            @Override
            public int compare(ContactEntity o1, ContactEntity o2) {
                int e1 = o1.getErrorCount();
                int e2 = o2.getErrorCount();
                
                if (e1 == 0 && e2 == 0) {
                    return LongevityUtils.TIMESTAMP_ASCENDING.compare(o1, o2);
                } else if (e1 == 0 && e2 != 0) {
                    return -1;
                } else if (e1 != 0 && e2 == 0) {
                    return 1;
                }
                
                // Sort everyone else from least recently failed to most recently failed.
                // TODO: Take the number of errors into account?
                long t1 = o1.getErrorTimeStamp();
                long t2 = o2.getErrorTimeStamp();
                return LongComparator.compare(t1, t2);
            }
        };
        
    private static final Comparator<ContactEntity> HEALTH_DESCENDING 
        = new ReverseComparator<ContactEntity>(HEALTH_ASCENDING);
    
    public static long getAdaptiveTimeout(Contact contact, 
            long defaultValue, TimeUnit unit) {
        long rtt = contact.getRoundTripTime(unit);
        if (rtt < 0) {
            return defaultValue;
        }
        
        return Math.min((long)(rtt * 1.5f), defaultValue);
    }
    
    
    
    public static ContactEntity[] byHealth(ContactEntity[] entities) {
        return byHealth(entities, true);
    }
    
    public static ContactEntity[] byHealth(ContactEntity[] entities, boolean ascending) {
        Arrays.sort(entities, ascending ? HEALTH_ASCENDING : HEALTH_DESCENDING);
        return entities;
    }
    
    /**
     * Turns the given array of {@link ContactEntity}s into an array of {@link Contact}s.
     */
    public static Contact[] toContacts(ContactEntity[] entities) {
        return toContacts(entities, 0, entities.length);
    }
    
    /**
     * Turns the given array of {@link ContactEntity}s into an array of {@link Contact}s.
     */
    public static Contact[] toContacts(ContactEntity[] entities, int offset, int length) {
        Contact[] contacts = new Contact[length];
        for (int i = 0; i < length; i++) {
            contacts[i] = entities[offset + i].getContact();
        }
        return contacts;
    }
}
