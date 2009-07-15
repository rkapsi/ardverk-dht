package com.ardverk.collection;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class FixedSizeHashSet<E> implements Set<E>, FixedSize, Serializable {
    
    private static final long serialVersionUID = -7937957462093712348L;

    private static final Object VALUE = new Object();
    
    private final FixedSizeHashMap<E, Object> map;

    public FixedSizeHashSet(int maxSize) {
        this(16, 0.75f, false, maxSize);
    }
    
    public FixedSizeHashSet(int initialSize, float loadFactor, int maxSize) {
        this(initialSize, loadFactor, false, maxSize);
    }
    
    public FixedSizeHashSet(int initialCapacity, float loadFactor, 
            boolean accessOrder, int maxSize) {
        
        map = new FixedSizeHashMap<E, Object>(
                initialCapacity, loadFactor, accessOrder, maxSize) {
            
            private static final long serialVersionUID = -2214875570521398012L;

            @Override
            protected boolean removeEldestEntry(Map.Entry<E, Object> eldest) {
                return FixedSizeHashSet.this.removeEldestEntry(eldest.getKey());
            }

            @Override
            protected void removing(Entry<E, Object> eldest) {
                FixedSizeHashSet.this.removing(eldest.getKey());
            }
        };
    }
    
    protected boolean removeEldestEntry(E eldest) {
        return size() > getMaxSize();
    }
    
    /**
     * 
     */
    protected void removing(E eldest) {
        // OVERRIDE
    }
    
    @Override
    public boolean add(E e) {
        return map.put(e, VALUE) == null;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean changed = false;
        for (E e : c) {
            changed |= add(e);
        }
        return changed;
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public boolean contains(Object o) {
        return map.keySet().contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return map.keySet().containsAll(c);
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    @Override
    public boolean remove(Object o) {
        return map.remove(o) != null;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return map.keySet().removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return map.keySet().retainAll(c);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public Object[] toArray() {
        return map.keySet().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return map.keySet().toArray(a);
    }

    @Override
    public int getMaxSize() {
        return map.getMaxSize();
    }

    @Override
    public boolean isFull() {
        return map.isFull();
    }
    
    @Override
    public String toString() {
        return map.keySet().toString();
    }
}
