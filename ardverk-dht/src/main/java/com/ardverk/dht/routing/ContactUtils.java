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
        
    public static long getAdaptiveTimeout(Contact2 contact, 
            long defaultValue, TimeUnit unit) {
        long rtt = contact.getRoundTripTime(unit);
        if (rtt < 0) {
            return defaultValue;
        }
        
        return Math.min((long)(rtt * 1.5f), defaultValue);
    }
    
    public static Contact2[] sort(Contact2[] contacts, KUID key) {
        return sort(contacts, key, true);
    }
    
    public static Contact2[] sort(Contact2[] contacts, KUID key, boolean ascending) {
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
    
    private static class ContactHandle {
        
        private final Contact2 contact;
        
        private final KUID xor;
        
        public ContactHandle(Contact2 contact, KUID key) {
            if (contact == null) {
                throw new NullArgumentException("contact");
            }
            
            this.contact = contact;
            this.xor = key.xor(contact.getContactId());
        }
    }
}
