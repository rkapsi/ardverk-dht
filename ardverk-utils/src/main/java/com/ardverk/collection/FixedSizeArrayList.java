package com.ardverk.collection;

import java.util.ArrayList;
import java.util.Collection;

public class FixedSizeArrayList<E> extends ArrayList<E> implements FixedSize {

    private static final long serialVersionUID = -8835179064931233224L;
    
    private final int maxSize;

    public FixedSizeArrayList(int maxSize) {
        super();
        
        if (maxSize < 0) {
            throw new IllegalArgumentException("maxSize=" + maxSize);
        }
        
        this.maxSize = maxSize;
    }

    public FixedSizeArrayList(Collection<? extends E> c, int maxSize) {
        super(c);
        
        if (maxSize < 0) {
            throw new IllegalArgumentException("maxSize=" + maxSize);
        }
        
        this.maxSize = maxSize;
    }

    public FixedSizeArrayList(int initialCapacity, int maxSize) {
        super(initialCapacity);
        
        if (maxSize < 0) {
            throw new IllegalArgumentException("maxSize=" + maxSize);
        }
        
        this.maxSize = maxSize;
    }
    
    @Override
    public int getMaxSize() {
        return maxSize;
    }
    
    @Override
    public boolean isFull() {
        return size() >= getMaxSize();
    }

    @Override
    public boolean add(E e) {
        adjustSize();
        return super.add(e);
    }

    @Override
    public void add(int index, E element) {
        adjustSize();
        super.add(index, element);
    }
    
    private void adjustSize() {
        if (isFull()) {
            E old = remove(0);
            removed(old);
        }
    }
    
    protected void removed(E element) {
        // OVERRIDE
    }
}
