package com.ardverk.dht.routing;

import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import org.ardverk.lang.NullArgumentException;

import com.ardverk.dht.KUID;

public class ContactUtils {

    private ContactUtils() {}
    
    private static final Comparator<ContactHandle> ASCENDING 
        = new Comparator<ContactHandle>() {
            @Override
            public int compare(ContactHandle o1, ContactHandle o2) {
                return o1.xor.compareTo(o2.xor);
            }
        };
    
    private static final Comparator<ContactHandle> DESCENDING 
        = new Comparator<ContactHandle>() {
            @Override
            public int compare(ContactHandle o1, ContactHandle o2) {
                return o2.xor.compareTo(o1.xor);
            }
        };
        
    public static long getAdaptiveTimeout(Contact contact, 
            long defaultValue, TimeUnit unit) {
        long rtt = contact.getRoundTripTime(unit);
        if (rtt < 0) {
            return defaultValue;
        }
        
        return Math.min((long)(rtt * 1.5f), defaultValue);
    }
    
    public static Contact[] sort(Contact[] contacts, KUID key) {
        return sort(contacts, key, true);
    }
    
    public static Contact[] sort(Contact[] contacts, KUID key, boolean ascending) {
        if (contacts == null) {
            throw new NullArgumentException("contacts");
        }
        
        if (key == null) {
            throw new NullArgumentException("key");
        }
        
        ContactHandle[] handles = new ContactHandle[contacts.length];
        for (int i = 0; i < contacts.length; i++) {
            handles[i] = new ContactHandle(contacts[i], key);
        }
        
        Arrays.sort(handles, ascending ? ASCENDING : DESCENDING);
        
        for (int i = 0; i < handles.length; i++) {
            contacts[i] = handles[i].contact;
        }
        
        return contacts;
    }
    
    /**
     * Returns {@code true} if both {@link Contact}s have the same {@link KUID}.
     */
    public static boolean hasSameContactId(Contact a, Contact b) {
        return a == b || a.getContactId().equals(b.getContactId());
    }
    
    private static class ContactHandle {
        
        private final Contact contact;
        
        private final KUID xor;
        
        public ContactHandle(Contact contact, KUID key) {
            if (contact == null) {
                throw new NullArgumentException("contact");
            }
            
            this.contact = contact;
            this.xor = key.xor(contact.getContactId());
        }
    }
}
