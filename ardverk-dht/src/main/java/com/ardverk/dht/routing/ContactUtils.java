package com.ardverk.dht.routing;

import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import com.ardverk.dht.KUID;

public class ContactUtils {

    private ContactUtils() {}
    
    private static final Comparator<ContactHandle> XOR_ASCENDING 
        = new Comparator<ContactHandle>() {
            @Override
            public int compare(ContactHandle o1, ContactHandle o2) {
                return o1.xor.compareTo(o2.xor);
            }
        };
    
    private static final Comparator<ContactHandle> XOR_DESCENDING 
        = new Comparator<ContactHandle>() {
            @Override
            public int compare(ContactHandle o1, ContactHandle o2) {
                return o2.xor.compareTo(o1.xor);
            }
        };
        
    private static final Comparator<Contact> TIME_ASCENDING 
        = new Comparator<Contact>() {
            @Override
            public int compare(Contact o1, Contact o2) {
                long t1 = o1.getTimeSinceLastContactInMillis();
                long t2 = o2.getTimeSinceLastContactInMillis();
                if (t1 < t2) {
                    return -1;
                } else if (t2 < t1) {
                    return 1;
                }
                return 0;
            }
        };
        
    private static final Comparator<Contact> TIME_DESCENDING 
        = new Comparator<Contact>() {
            @Override
            public int compare(Contact o1, Contact o2) {
                long t1 = o1.getTimeSinceLastContactInMillis();
                long t2 = o2.getTimeSinceLastContactInMillis();
                if (t1 < t2) {
                    return 1;
                } else if (t2 < t1) {
                    return -1;
                }
                return 0;
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
    
    public static Contact[] sortByXor(Contact[] contacts, KUID key) {
        return sortByXor(contacts, key, true);
    }
    
    public static Contact[] sortByXor(Contact[] contacts, KUID key, boolean ascending) {
        ContactHandle[] handles = new ContactHandle[contacts.length];
        for (int i = 0; i < contacts.length; i++) {
            handles[i] = new ContactHandle(contacts[i], key);
        }
        
        Arrays.sort(handles, ascending ? XOR_ASCENDING : XOR_DESCENDING);
        
        for (int i = 0; i < handles.length; i++) {
            contacts[i] = handles[i].contact;
        }
        
        return contacts;
    }
    
    public static Contact[] sortByTime(Contact[] contacts) {
        return sortByTime(contacts, true);
    }
    
    public static Contact[] sortByTime(Contact[] contacts, boolean ascending) {
        Arrays.sort(contacts, ascending ? TIME_ASCENDING : TIME_DESCENDING);
        return contacts;
    }
    
    private static class ContactHandle {
        
        private final Contact contact;
        
        private final KUID xor;
        
        public ContactHandle(Contact contact, KUID key) {
            this.contact = contact;
            this.xor = key.xor(contact.getContactId());
        }
    }
}
