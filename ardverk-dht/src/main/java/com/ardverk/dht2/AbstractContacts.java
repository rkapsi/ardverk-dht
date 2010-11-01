package com.ardverk.dht2;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.ardverk.dht.routing.Contact;

public abstract class AbstractContacts implements Contacts {

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }
    
    @Override
    public Iterator<Contact> iterator() {
        return new Iterator<Contact>() {

            private int index = 0;
            
            @Override
            public boolean hasNext() {
                return index < size();
            }

            @Override
            public Contact next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                
                return get(index++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
